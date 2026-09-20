package dev.recallforge.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.web.SecurityFilterChain;

import dev.recallforge.exception.LoginDisabledException;
import dev.recallforge.security.DatabaseOAuth2UserService;
import dev.recallforge.security.DatabaseOidcUserService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Builds the security filter chain with three distinct modes, checked in
 * order: {@link AppSecurityProperties#enabled()} off -&gt; everything
 * permitted (local/dev bypass); {@link AppLoginProperties#enabled()} off
 * -&gt; existing sessions keep working but no new login is possible;
 * otherwise -&gt; normal OAuth2 login via {@link DatabaseOAuth2UserService}
 * (GitHub) / {@link DatabaseOidcUserService} (Google). See the inline
 * comments in {@link #securityFilterChain} for why CSRF is disabled and why
 * {@code /oauth2/**} is deliberately excluded from the login-disabled
 * permit-list.
 */
@Configuration
@EnableConfigurationProperties({
    AppSecurityProperties.class,
    AppLoginProperties.class
})
public class SecurityConfig {

    private final DatabaseOAuth2UserService databaseOAuth2UserService;
    private final DatabaseOidcUserService databaseOidcUserService;

    public SecurityConfig(
        DatabaseOAuth2UserService databaseOAuth2UserService,
        DatabaseOidcUserService databaseOidcUserService
    ) {
        this.databaseOAuth2UserService = databaseOAuth2UserService;
        this.databaseOidcUserService = databaseOidcUserService;
    }

    /**
     * Works around two gaps in LinkedIn's "Sign In with LinkedIn using OpenID
     * Connect" implementation, for the {@code linkedin} registration only —
     * Google and GitHub are untouched:
     *
     * <ul>
     *   <li><b>PKCE</b> — Spring Security adds PKCE to every
     *   {@code authorization_code} request by default
     *   ({@code DefaultOAuth2AuthorizationRequestResolver.DEFAULT_PKCE_APPLIER},
     *   which runs before this customizer). LinkedIn's token endpoint responds
     *   {@code invalid_client} when a token request carries a {@code code_verifier}
     *   alongside {@code client_secret_post} authentication.</li>
     *   <li><b>Nonce</b> — for {@code openid} scope Spring sends a {@code nonce}
     *   and requires it back in the ID token; LinkedIn's ID token does not carry
     *   it, which fails as {@code invalid_nonce}. Removing the {@code nonce}
     *   request attribute makes {@code OidcAuthorizationCodeAuthenticationProvider}
     *   skip that check (the ID token is still signature/issuer/exp validated via
     *   {@code jwk-set-uri}).</li>
     * </ul>
     *
     * <p>Built inline (rather than as a bean) so the login-disabled filter chain,
     * which runs without an OAuth2 client auto-configuration and therefore without
     * a {@link ClientRegistrationRepository}, still loads.
     */
    private static OAuth2AuthorizationRequestResolver linkedInCompatResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver resolver =
            new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");

        resolver.setAuthorizationRequestCustomizer(builder -> builder.attributes(attrs -> {
            if ("linkedin".equals(attrs.get(OAuth2ParameterNames.REGISTRATION_ID))) {
                attrs.remove(PkceParameterNames.CODE_VERIFIER);
                attrs.remove(OidcParameterNames.NONCE);
                builder.additionalParameters(params -> {
                    params.remove(PkceParameterNames.CODE_CHALLENGE);
                    params.remove(PkceParameterNames.CODE_CHALLENGE_METHOD);
                    params.remove(OidcParameterNames.NONCE);
                });
            }
        }));

        return resolver;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AppSecurityProperties props,
            AppLoginProperties loginProperties,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository
    ) throws Exception {

        // CSRF tokens are unnecessary here: the session cookie (see application.yml)
        // is SameSite=Lax, so browsers withhold it on cross-site POST/PUT/DELETE
        // requests, which already blocks the classic CSRF attack on our mutating endpoints.
        http.csrf(csrf -> csrf.disable());

        if (!props.enabled()) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        if (!loginProperties.enabled()) {
            // Login is administratively disabled: existing sessions keep working (session
            // restoration doesn't go through oauth2Login()), but there's no mechanism left
            // to establish a new one. /login stays reachable so LoginController can reject
            // it with the same LoginDisabledException/403 used by the REST API; /oauth2/**
            // and /login/oauth2/** are deliberately left out of permitAll so a client can't
            // bypass the disabled login page by hitting the provider URL directly — they
            // fall through to anyRequest().authenticated() below, which no one can ever
            // satisfy without a login mechanism.
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/",
                        "/login",
                        "/login.html",
                        "/terms",
                        "/terms.html",
                        "/privacy",
                        "/privacy.html",
                        "/recall-forge.png",
                        "/css/**",
                        "/js/**",
                        "/images/**"
                    ).permitAll()
                    .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                    (request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("text/plain");
                        response.getWriter().write(new LoginDisabledException().getMessage());
                    }
                ));

            return http.build();
        }

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/login",
                    "/login.html",
                    "/terms",
                    "/terms.html",
                    "/privacy",
                    "/privacy.html",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/recall-forge.png", // icon
                    "/css/**",
                    "/js/**",
                     "/images/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .authorizationEndpoint(endpoint -> endpoint
                    .authorizationRequestResolver(
                        linkedInCompatResolver(clientRegistrationRepository.getObject())
                    )
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(databaseOAuth2UserService)     // GitHub
                    .oidcUserService(databaseOidcUserService)   // Google
                )
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
            );

        return http.build();
    }
}
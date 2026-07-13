package dev.recallforge.config;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;

import dev.recallforge.security.DatabaseOAuth2UserService;
import dev.recallforge.security.DatabaseOidcUserService;
import dev.recallforge.exception.LoginDisabledException;

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

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AppSecurityProperties props,
            AppLoginProperties loginProperties
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
                        "/js/**"
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
                    "/js/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
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
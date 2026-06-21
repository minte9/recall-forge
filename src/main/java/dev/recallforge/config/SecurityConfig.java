package dev.recallforge.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;

import dev.recallforge.security.DatabaseOAuth2UserService;
import dev.recallforge.security.DatabaseOidcUserService;

@Configuration
@EnableConfigurationProperties(AppSecurityProperties.class)
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
            AppSecurityProperties props
    ) throws Exception {

        http.csrf(csrf -> csrf.disable());

        if (!props.enabled()) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/recall-forge.png", // icon
                    "/css/**",
                    "/js/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            // .formLogin(form -> form
            //     .defaultSuccessUrl("/", true)
            // )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .oauth2Login(oauth2 -> oauth2
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

    @Bean
    public InMemoryUserDetailsManager userDetailsService(
            AppSecurityProperties props,
            PasswordEncoder passwordEncoder) {

        UserDetails user = User.withUsername(props.username())
            .password(passwordEncoder.encode(props.password()))
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
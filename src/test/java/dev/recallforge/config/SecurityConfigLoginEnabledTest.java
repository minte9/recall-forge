package dev.recallforge.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import dev.recallforge.controller.LoginController;
import dev.recallforge.security.DatabaseOAuth2UserService;
import dev.recallforge.security.DatabaseOidcUserService;

// Regression check: login enabled (the default) must keep working as before.
// OAuth2ClientAutoConfiguration is deliberately kept active here (unlike the
// disabled-login test) because .oauth2Login() needs a real ClientRegistrationRepository
// to build; fake client-id/secret are supplied below since real ones only exist in .env.
@WebMvcTest(
    controllers = LoginController.class,
    excludeAutoConfiguration = {
        OAuth2ClientWebSecurityAutoConfiguration.class
    }
)
@EnableWebSecurity
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "app.login.enabled=true",
    "spring.security.oauth2.client.registration.google.client-id=test-client-id",
    "spring.security.oauth2.client.registration.google.client-secret=test-client-secret",
    "spring.security.oauth2.client.registration.github.client-id=test-client-id",
    "spring.security.oauth2.client.registration.github.client-secret=test-client-secret"
})
class SecurityConfigLoginEnabledTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private DatabaseOAuth2UserService databaseOAuth2UserService;

    @MockitoBean
    private DatabaseOidcUserService databaseOidcUserService;

    @Test
    void getLogin_forwardsToLoginPage() {
        mockMvcTester.get().uri("/login")
            .assertThat()
            .hasStatusOk();
    }

    @Test
    void oauth2AuthorizationEndpoint_redirectsToProvider() {
        mockMvcTester.get().uri("/oauth2/authorization/google")
            .assertThat()
            .hasStatus(HttpStatus.FOUND);
    }
}

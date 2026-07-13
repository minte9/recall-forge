package dev.recallforge.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
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

@WebMvcTest(
    controllers = LoginController.class,
    excludeAutoConfiguration = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
    }
)
@EnableWebSecurity
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.login.enabled=false")
class SecurityConfigLoginDisabledTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private DatabaseOAuth2UserService databaseOAuth2UserService;

    @MockitoBean
    private DatabaseOidcUserService databaseOidcUserService;

    @Test
    void getLogin_returnsForbiddenWithDisabledMessage() {
        mockMvcTester.get().uri("/login")
            .assertThat()
            .hasStatus(HttpStatus.FORBIDDEN)
            .bodyText().isEqualTo("Login is temporarily disabled.");
    }

    @Test
    void oauth2AuthorizationEndpoint_isBlocked() {
        mockMvcTester.get().uri("/oauth2/authorization/google")
            .assertThat()
            .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void oauth2CallbackEndpoint_isBlocked() {
        mockMvcTester.get().uri("/login/oauth2/code/google")
            .assertThat()
            .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void publicPages_stillAccessible() {
        mockMvcTester.get().uri("/terms")
            .assertThat()
            .hasStatusOk();

        mockMvcTester.get().uri("/privacy")
            .assertThat()
            .hasStatusOk();
    }
}

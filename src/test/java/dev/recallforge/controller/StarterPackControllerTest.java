package dev.recallforge.controller;

import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import dev.recallforge.service.StarterPackService;

@WebMvcTest(
    controllers = StarterPackController.class,
    excludeAutoConfiguration = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
class StarterPackControllerTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private StarterPackService starterPackService;

    @Test
    void getDuePercentages_returnsServiceMapAsJson() {
        when(starterPackService.getDuePercentages()).thenReturn(Map.of("Java", 12));

        mockMvcTester.get().uri("/api/starter-packs/due")
            .assertThat()
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .extractingPath("$.Java")
            .isEqualTo(12);
    }
}

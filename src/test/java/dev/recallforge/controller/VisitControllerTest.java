package dev.recallforge.controller;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import dev.recallforge.service.PageVisitService;

@WebMvcTest(
    controllers = VisitController.class,
    excludeAutoConfiguration = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
class VisitControllerTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private PageVisitService pageVisitService;

    @Test
    void recordVisit_withoutBody_passesClientIpAndUserAgentToService_returnsNoContent() {
        mockMvcTester.post().uri("/api/visits")
            .header("User-Agent", "Mozilla/5.0 Test")
            .with(request -> {
                request.setRemoteAddr("203.0.113.7");
                return request;
            })
            .assertThat()
            .hasStatus(HttpStatus.NO_CONTENT);

        verify(pageVisitService).recordVisit("203.0.113.7", "Mozilla/5.0 Test", null, "localhost");
    }

    @Test
    void recordVisit_withReferrerBody_passesReferrerAndServerNameToService() {
        mockMvcTester.post().uri("https://recallforge.dev/api/visits")
            .header("User-Agent", "Mozilla/5.0 Test")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                { "referrer": "https://www.google.com/" }
                """)
            .with(request -> {
                request.setRemoteAddr("203.0.113.7");
                return request;
            })
            .assertThat()
            .hasStatus(HttpStatus.NO_CONTENT);

        verify(pageVisitService).recordVisit(
            "203.0.113.7", "Mozilla/5.0 Test", "https://www.google.com/", "recallforge.dev");
    }
}

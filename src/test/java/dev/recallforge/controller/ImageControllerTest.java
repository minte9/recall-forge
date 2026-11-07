package dev.recallforge.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import dev.recallforge.domain.ImageAsset;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.security.CurrentUserService;
import dev.recallforge.service.ImageService;

@WebMvcTest(
    controllers = ImageController.class,
    excludeAutoConfiguration = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
class ImageControllerTest {

    private static final String SHA = "a".repeat(64);

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private ImageService imageService;

    @MockitoBean
    private CurrentUserService currentUserService;

    private ImageAsset image(String filename) {
        return new ImageAsset(filename, "image/png", SHA, new byte[]{1, 2, 3}, new UserAccount());
    }

    @Test
    void getImage_found_returnsBytesWithSafeHeaders() {
        when(imageService.find("Pasted image 1.png")).thenReturn(Optional.of(image("Pasted image 1.png")));

        MvcTestResult result = mockMvcTester.get().uri("/api/images/{filename}", "Pasted image 1.png").exchange();

        assertThat(result).hasStatusOk()
            .hasContentType("image/png")
            .hasHeader("X-Content-Type-Options", "nosniff")
            .hasHeader("Content-Security-Policy", "default-src 'none'; sandbox")
            .hasHeader("ETag", "\"" + SHA + "\"");
        assertThat(result.getResponse().getHeader("Cache-Control")).contains("private");
        assertThat(result.getResponse().getContentAsByteArray()).containsExactly(1, 2, 3);
    }

    @Test
    void getImage_matchingEtag_returnsNotModifiedWithoutBody() {
        when(imageService.find("a.png")).thenReturn(Optional.of(image("a.png")));

        MvcTestResult result = mockMvcTester.get().uri("/api/images/a.png")
            .header("If-None-Match", "\"" + SHA + "\"")
            .exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_MODIFIED);
        assertThat(result.getResponse().getContentAsByteArray()).isEmpty();
    }

    @Test
    void getImage_missing_returns404() {
        when(imageService.find("nope.png")).thenReturn(Optional.empty());

        assertThat(mockMvcTester.get().uri("/api/images/nope.png")).hasStatus(HttpStatus.NOT_FOUND);
    }
}

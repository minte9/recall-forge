package dev.recallforge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import dev.recallforge.dto.MarkdownUploadResponse;
import dev.recallforge.service.MarkdownFileService;

@WebMvcTest(
    controllers = MarkdownController.class,
    excludeAutoConfiguration = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
class MarkdownControllerTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private MarkdownFileService markdownFileService;

    @Test
    void uploadMarkdown_delegatesToServiceAndReturnsResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "notes.md", "text/markdown", "### Topic\ncontent".getBytes()
        );
        when(markdownFileService.upload(any())).thenReturn(
            new MarkdownUploadResponse(1L, "notes.md", "### Topic\ncontent")
        );

        mockMvcTester.post().uri("/api/markdown/upload").multipart().file(file)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "markdownFileId": 1,
                  "filename": "notes.md",
                  "content": "### Topic\\ncontent"
                }
                """);

        verify(markdownFileService).upload(any());
    }

    @Test
    void updateMarkdown_validRequest_delegatesToServiceAndReturnsResponse() throws Exception {
        when(markdownFileService.update(1L, "### Topic\nupdated content")).thenReturn(
            new MarkdownUploadResponse(1L, "notes.md", "### Topic\nupdated content")
        );

        mockMvcTester.put().uri("/api/markdown/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "content": "### Topic\\nupdated content"
                }
                """)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "markdownFileId": 1,
                  "filename": "notes.md",
                  "content": "### Topic\\nupdated content"
                }
                """);

        verify(markdownFileService).update(1L, "### Topic\nupdated content");
    }

    @Test
    void updateMarkdown_blankContent_returnsBadRequestAndSkipsService() throws Exception {
        mockMvcTester.put().uri("/api/markdown/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "content": ""
                }
                """)
            .assertThat()
            .hasStatus4xxClientError();

        verify(markdownFileService, never()).update(any(), any());
    }
}

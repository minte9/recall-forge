package dev.recallforge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

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

import dev.recallforge.dto.CodeChallengeResponse;
import dev.recallforge.dto.MarkdownUploadResponse;
import dev.recallforge.exception.MarkdownFileHasReviewsException;
import dev.recallforge.security.CurrentUserService;
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

    @MockitoBean
    private CurrentUserService currentUserService;

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

    @Test
    void resetMarkdownReviews_delegatesToCurrentUserServiceAndReturnsNoContent() throws Exception {
        mockMvcTester.delete().uri("/api/markdown/1/reviews")
            .assertThat()
            .hasStatus(204);

        verify(currentUserService).resetCurrentUserReviewsForMarkdownFile(1L);
    }

    @Test
    void getMarkdownReviewCount_delegatesToServiceAndReturnsCount() throws Exception {
        when(markdownFileService.countReviews(1L)).thenReturn(3L);

        mockMvcTester.get().uri("/api/markdown/1/reviews/count")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "count": 3
                }
                """);
    }

    @Test
    void getMarkdownCodeChallenges_delegatesToServiceAndReturnsList() throws Exception {
        when(markdownFileService.getCodeChallenges(1L)).thenReturn(List.of(
            new CodeChallengeResponse(
                10L, "Arrays", "Write a method that reverses an array.", "answer", "Good", 0.9,
                LocalDateTime.of(2026, 7, 1, 10, 0))
        ));

        mockMvcTester.get().uri("/api/markdown/1/reviews/code-challenges")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                [
                  {
                    "topicId": 10,
                    "topicTitle": "Arrays",
                    "question": "Write a method that reverses an array.",
                    "userAnswer": "answer",
                    "feedback": "Good",
                    "score": 0.9
                  }
                ]
                """);
    }

    @Test
    void deleteMarkdown_delegatesToServiceAndReturnsNoContent() throws Exception {
        mockMvcTester.delete().uri("/api/markdown/1")
            .assertThat()
            .hasStatus(204);

        verify(markdownFileService).delete(1L);
    }

    @Test
    void deleteMarkdown_reviewsStillExist_returnsConflict() throws Exception {
        org.mockito.Mockito.doThrow(new MarkdownFileHasReviewsException(
                "Reset reviews for this markdown file before deleting it."))
            .when(markdownFileService).delete(1L);

        mockMvcTester.delete().uri("/api/markdown/1")
            .assertThat()
            .hasStatus(409);
    }
}

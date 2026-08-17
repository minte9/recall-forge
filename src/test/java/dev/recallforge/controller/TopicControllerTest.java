package dev.recallforge.controller;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import dev.recallforge.domain.Topic;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.dto.ReviewHistoryResponse;
import dev.recallforge.security.CurrentUserService;
import dev.recallforge.service.ReviewService;
import dev.recallforge.service.TopicService;

@WebMvcTest(
    controllers = TopicController.class,
    excludeAutoConfiguration = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
class TopicControllerTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private TopicService topicService;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private CurrentUserService currentUserService;

    private static Topic topicFixture(long id, String title, double memoryScore, LocalDateTime nextReviewAt) {
        Topic topic = new Topic(
            "prod", "backend", "spring", "notes.md",
            title, "content", null, new UserAccount()
        );
        ReflectionTestUtils.setField(topic, "id", id);
        ReflectionTestUtils.setField(topic, "memoryScore", memoryScore);
        ReflectionTestUtils.setField(topic, "nextReviewAt", nextReviewAt);
        return topic;
    }

    @Test
    void getTopics_returnsTopicsForCurrentUserAsJson() {
        when(currentUserService.currentUserId()).thenReturn(42L);
        when(topicService.getAllTopics(42L)).thenReturn(List.of(
            topicFixture(1L, "Topic One", 0.75, LocalDateTime.of(2026, 7, 10, 9, 0))
        ));

        mockMvcTester.get().uri("/api/topics")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                [
                  {
                    "id": 1,
                    "title": "Topic One",
                    "memoryScore": 0.75,
                    "nextReviewAt": "2026-07-10T09:00:00Z"
                  }
                ]
                """);

        verify(topicService).getAllTopics(42L);
    }

    @Test
    void getTopics_serviceReturnsEmptyList_returnsEmptyJsonArray() {
        when(currentUserService.currentUserId()).thenReturn(42L);
        when(topicService.getAllTopics(42L)).thenReturn(List.of());

        mockMvcTester.get().uri("/api/topics")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("[]");
    }

    @Test
    void getDueTopics_returnsDueTopicsForCurrentUserAsJson() {
        when(currentUserService.currentUserId()).thenReturn(7L);
        when(topicService.getDueTopics(7L, "Java")).thenReturn(List.of(
            topicFixture(2L, "Topic Two", 0.3, LocalDateTime.of(2026, 7, 5, 8, 30))
        ));

        mockMvcTester.get().uri("/api/topics/due")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                [
                  {
                    "id": 2,
                    "title": "Topic Two",
                    "memoryScore": 0.3,
                    "nextReviewAt": "2026-07-05T08:30:00Z"
                  }
                ]
                """);

        verify(topicService).getDueTopics(7L, "Java");
    }

    @Test
    void getDueTopics_serviceReturnsEmptyList_returnsEmptyJsonArray() {
        when(currentUserService.currentUserId()).thenReturn(7L);
        when(topicService.getDueTopics(7L, "Java")).thenReturn(List.of());

        mockMvcTester.get().uri("/api/topics/due")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("[]");
    }

    @Test
    void getTopicsByMarkdownFile_returnsTopicsForCurrentUserAsJson() {
        when(currentUserService.currentUserId()).thenReturn(7L);
        when(topicService.getTopicsByMarkdownFileId(3L, 7L)).thenReturn(List.of(
            topicFixture(1L, "Topic One", 0.75, LocalDateTime.of(2026, 7, 10, 9, 0)),
            topicFixture(2L, "Topic Two", 0.3, LocalDateTime.of(2026, 7, 5, 8, 30))
        ));

        mockMvcTester.get().uri("/api/topics/by-markdown-file/3")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                [
                  {
                    "id": 1,
                    "title": "Topic One",
                    "memoryScore": 0.75,
                    "nextReviewAt": "2026-07-10T09:00:00Z"
                  },
                  {
                    "id": 2,
                    "title": "Topic Two",
                    "memoryScore": 0.3,
                    "nextReviewAt": "2026-07-05T08:30:00Z"
                  }
                ]
                """);

        verify(topicService).getTopicsByMarkdownFileId(3L, 7L);
    }

    @Test
    void getTopicsByMarkdownFile_serviceReturnsEmptyList_returnsEmptyJsonArray() {
        when(currentUserService.currentUserId()).thenReturn(7L);
        when(topicService.getTopicsByMarkdownFileId(3L, 7L)).thenReturn(List.of());

        mockMvcTester.get().uri("/api/topics/by-markdown-file/3")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("[]");
    }

    @Test
    void getReviewHistory_returnsHistoryFromReviewService() {
        when(reviewService.getReviewHistoryForTopic(5L)).thenReturn(List.of(
            new ReviewHistoryResponse(
                1L, 5L, "Topic Five", "What is X?", "X is Y",
                0.9, "Great answer", LocalDateTime.of(2026, 7, 1, 12, 0)
            )
        ));

        mockMvcTester.get().uri("/api/topics/5/reviews")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                [
                  {
                    "id": 1,
                    "topicId": 5,
                    "topicTitle": "Topic Five",
                    "question": "What is X?",
                    "userAnswer": "X is Y",
                    "score": 0.9,
                    "feedback": "Great answer",
                    "reviewedAt": "2026-07-01T12:00:00Z"
                  }
                ]
                """);

        verify(reviewService).getReviewHistoryForTopic(5L);
    }

    @Test
    void getReviewHistory_serviceReturnsEmptyList_returnsEmptyJsonArray() {
        when(reviewService.getReviewHistoryForTopic(5L)).thenReturn(List.of());

        mockMvcTester.get().uri("/api/topics/5/reviews")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("[]");
    }
}

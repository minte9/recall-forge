package dev.recallforge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import dev.recallforge.domain.RetryType;
import dev.recallforge.dto.AnswerResponse;
import dev.recallforge.dto.FeedbackHistoryResponse;
import dev.recallforge.dto.ReviewQuestionResponse;
import dev.recallforge.dto.ReviewQueueResponse;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.service.ReviewService;

@WebMvcTest(
    controllers = ReviewController.class,
    excludeAutoConfiguration = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    void startReview_withMarkdownFileId_delegatesToServiceAndReturnsQuestion() {
        ReviewQuestionResponse response = new ReviewQuestionResponse(
            1L, "Topic One", "What is X?", "# markdown content", 7L, false, null, false, false
        );
        when(reviewService.startReview(7L, "Java", null)).thenReturn(response);

        mockMvcTester.post().uri("/api/reviews/start?markdownFileId=7")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "topicId": 1,
                  "topicTitle": "Topic One",
                  "question": "What is X?",
                  "markdownContent": "# markdown content",
                  "markdownFileId": 7,
                  "done": false,
                  "message": null
                }
                """);

        verify(reviewService).startReview(7L, "Java", null);
    }

    @Test
    void startReview_withoutMarkdownFileId_passesNullToService() {
        when(reviewService.startReview(isNull(), eq("Java"), isNull())).thenReturn(ReviewQuestionResponse.noReviewsDue());

        mockMvcTester.post().uri("/api/reviews/start")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "topicId": null,
                  "topicTitle": null,
                  "question": null,
                  "markdownContent": null,
                  "markdownFileId": null,
                  "done": true,
                  "message": "All reviews are done for now. Come back later."
                }
                """);

        verify(reviewService).startReview(isNull(), eq("Java"), isNull());
    }

    @Test
    void startReview_withTopicId_passesItThroughToService() {
        ReviewQuestionResponse response = new ReviewQuestionResponse(
            42L, "Topic Forty-Two", "What is Y?", "# markdown content", 7L, false, null, false, false
        );
        when(reviewService.startReview(7L, "Java", 42L)).thenReturn(response);

        mockMvcTester.post().uri("/api/reviews/start?markdownFileId=7&topicId=42")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "topicId": 42,
                  "topicTitle": "Topic Forty-Two",
                  "question": "What is Y?",
                  "markdownContent": "# markdown content",
                  "markdownFileId": 7,
                  "done": false,
                  "message": null
                }
                """);

        verify(reviewService).startReview(7L, "Java", 42L);
    }

    @Test
    void answerQuestion_validRequest_returnsScoreAndReward() {
        RewardResponse reward = new RewardResponse(
            4, "backend", 6L, 10L, 60
        );
        AnswerResponse response = new AnswerResponse(
            1L, "Topic One", 0.8, "Good job", 0.65,
            LocalDateTime.of(2026, 7, 6, 9, 0), 2, reward, 3L
        );
        when(reviewService.answerQuestion(eq(1L), any(), any(), anyBoolean(), anyBoolean())).thenReturn(response);

        mockMvcTester.post().uri("/api/reviews/answer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "topicId": 1,
                  "question": "What is X?",
                  "userAnswer": "X is Y",
                  "isRetry": false,
                  "codeChallenge": false
                }
                """)
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "topicId": 1,
                  "topicTitle": "Topic One",
                  "score": 0.8,
                  "feedback": "Good job",
                  "updateMemoryScore": 0.65,
                  "dueInSubcategory": 3,
                  "reward": {
                    "streakDays": 4,
                    "masteryScope": "backend",
                    "masteredTopics": 6,
                    "totalTopicsInScope": 10,
                    "masteryPercent": 60
                  }
                }
                """);

        verify(reviewService).answerQuestion(1L, "What is X?", "X is Y", false, false);
    }

    @Test
    void answerQuestion_blankFields_returnsBadRequestAndSkipsService() {
        mockMvcTester.post().uri("/api/reviews/answer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "topicId": null,
                  "question": "",
                  "userAnswer": ""
                }
                """)
            .assertThat()
            .hasStatus4xxClientError();

        verify(reviewService, never()).answerQuestion(any(), any(), any(), anyBoolean(), anyBoolean());
    }

    @Test
    void startCodeChallenge_delegatesToServiceAndReturnsQuestion() {
        ReviewQuestionResponse response = new ReviewQuestionResponse(
            1L, "Topic One", "Write a function that reverses a string.",
            "# markdown content", 7L, false, null, true, true
        );
        when(reviewService.startCodeChallenge(1L)).thenReturn(response);

        mockMvcTester.post().uri("/api/reviews/1/code-challenge")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "topicId": 1,
                  "topicTitle": "Topic One",
                  "question": "Write a function that reverses a string.",
                  "markdownContent": "# markdown content",
                  "markdownFileId": 7,
                  "done": false,
                  "message": null,
                  "forcePractice": true,
                  "codeChallenge": true
                }
                """);

        verify(reviewService).startCodeChallenge(1L);
    }

    @Test
    void getDailyQueue_withMarkdownFileId_delegatesToService() {
        ReviewQueueResponse response = new ReviewQueueResponse(
            3L, LocalDateTime.of(2026, 7, 6, 8, 0), false
        );
        when(reviewService.getDailyQueue(5L, "Java")).thenReturn(response);

        mockMvcTester.get().uri("/api/reviews/queue/today?markdownFileId=5")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                  "dueCount": 3,
                  "nextReviewAt": "2026-07-06T08:00:00",
                  "doneForToday": false
                }
                """);

        verify(reviewService).getDailyQueue(5L, "Java");
    }

    @Test
    void getFeedbackHistory_returnsListFromService() {
        List<FeedbackHistoryResponse> history = List.of(
            new FeedbackHistoryResponse(
                1L, "What is X?", "X is Y", "Good job",
                LocalDateTime.of(2026, 7, 1, 10, 0), 0.8, false, RetryType.REVIEW
            )
        );
        when(reviewService.getFeedbackHistory(9L)).thenReturn(history);

        mockMvcTester.get().uri("/api/reviews/9/feedback-history")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""
                [
                  {
                    "id": 1,
                    "question": "What is X?",
                    "userAnswer": "X is Y",
                    "feedback": "Good job",
                    "createdAt": "2026-07-01T10:00:00Z",
                    "score": 0.8,
                    "isRetry": false
                  }
                ]
                """);
    }

    @Test
    void getFeedbackHistory_serviceReturnsEmptyList_returnsEmptyJsonArray() {
        when(reviewService.getFeedbackHistory(9L)).thenReturn(List.of());

        mockMvcTester.get().uri("/api/reviews/9/feedback-history")
            .assertThat()
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("[]");
    }
}

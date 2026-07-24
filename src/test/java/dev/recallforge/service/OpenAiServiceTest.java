package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Does not exercise the real OpenAI network call: RestClient is mocked.
 * Covers only prompt trimming and score security limits, as requested.
 */
@ExtendWith(MockitoExtension.class)
class OpenAiServiceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private OpenAiService openAiService;

    @BeforeEach
    void setUp() {
        openAiService = new OpenAiService(restClient, "gpt-test");
    }

    private void mockAiReply(String replyContent) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(Object.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        String responseJson = """
                {"choices":[{"message":{"content":%s}}]}
                """.formatted(JSON.writeValueAsString(replyContent));

        when(responseSpec.body(JsonNode.class)).thenReturn(JSON.readTree(responseJson));
    }

    @SuppressWarnings("unchecked")
    private String capturePromptSentToAi() {
        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(requestBodyUriSpec).body(bodyCaptor.capture());

        Map<String, Object> requestBody = (Map<String, Object>) bodyCaptor.getValue();
        Object[] messages = (Object[]) requestBody.get("messages");
        Map<String, Object> message = (Map<String, Object>) messages[0];
        return (String) message.get("content");
    }

    // --- trimTopicContent, exercised through generateQuestion ---

    @Test
    void generateQuestion_shortTopicContent_isKeptAsIsAndNotTruncated() {
        mockAiReply("Some question?");

        openAiService.generateQuestion("Title", "  Short topic content  ", 0.5, List.of());

        String prompt = capturePromptSentToAi();
        assertThat(prompt).contains("Short topic content");
        assertThat(prompt).doesNotContain("[truncated]");
    }

    @Test
    void generateQuestion_longTopicContent_isTruncatedAtMaxCharsWithMarker() {
        mockAiReply("Some question?");
        String longContent = "0123456789".repeat(350); // 3500 chars
        String expectedTruncated = longContent.substring(0, 3000) + "\n...[truncated]";

        openAiService.generateQuestion("Title", longContent, 0.5, List.of());

        String prompt = capturePromptSentToAi();
        assertThat(prompt).contains(expectedTruncated);
        assertThat(prompt).doesNotContain(longContent);
    }

    @Test
    void generateQuestion_nullTopicContent_doesNotThrowAndIsTreatedAsEmpty() {
        mockAiReply("Some question?");

        assertThat(openAiService.generateQuestion("Title", null, 0.5, List.of())).isEqualTo("Some question?");

        String prompt = capturePromptSentToAi();
        assertThat(prompt).doesNotContain("null");
    }

    // --- proficiency guidance derived from memoryScore ---

    @Test
    void generateQuestion_memoryScoreBelowThreshold_labelsProficiencyEasy() {
        mockAiReply("Some question?");

        openAiService.generateQuestion("Title", "Content", 0.69, List.of());

        String prompt = capturePromptSentToAi();
        assertThat(prompt).contains("User's proficiency on this topic: EASY");
    }

    @Test
    void generateQuestion_memoryScoreAtThreshold_labelsProficiencyMedium() {
        mockAiReply("Some question?");

        openAiService.generateQuestion("Title", "Content", 0.7, List.of());

        String prompt = capturePromptSentToAi();
        assertThat(prompt).contains("User's proficiency on this topic: MEDIUM");
    }

    @Test
    void generateQuestion_memoryScoreAboveThreshold_labelsProficiencyMedium() {
        mockAiReply("Some question?");

        openAiService.generateQuestion("Title", "Content", 0.95, List.of());

        String prompt = capturePromptSentToAi();
        assertThat(prompt).contains("User's proficiency on this topic: MEDIUM");
    }

    // --- recent review history included in prompt ---

    @Test
    void generateQuestion_noRecentReviews_omitsRecentlyAskedSection() {
        mockAiReply("Some question?");

        openAiService.generateQuestion("Title", "Content", 0.5, List.of());

        String prompt = capturePromptSentToAi();
        assertThat(prompt).doesNotContain("Recently asked questions");
    }

    @Test
    void generateQuestion_withRecentReviews_listsThemAndAsksToAvoidRepeats() {
        mockAiReply("Some question?");
        List<ReviewSummary> recentReviews = List.of(
            new ReviewSummary("What is X?", 0.8, "Good understanding"),
            new ReviewSummary("What is Y?", 0.3, "Missed the point"));

        openAiService.generateQuestion("Title", "Content", 0.5, recentReviews);

        String prompt = capturePromptSentToAi();
        assertThat(prompt).contains("Recently asked questions on this topic (avoid repeating these):");
        assertThat(prompt).contains("\"What is X?\" (score 0.80, feedback: Good understanding)");
        assertThat(prompt).contains("\"What is Y?\" (score 0.30, feedback: Missed the point)");
        assertThat(prompt).contains("Do not ask a question that duplicates or closely rephrases");
    }

    @Test
    void generateQuestion_recentReviewWithBlankFeedback_omitsFeedbackSuffix() {
        mockAiReply("Some question?");
        List<ReviewSummary> recentReviews = List.of(new ReviewSummary("What is X?", 0.8, "  "));

        openAiService.generateQuestion("Title", "Content", 0.5, recentReviews);

        String prompt = capturePromptSentToAi();
        assertThat(prompt).contains("\"What is X?\" (score 0.80)");
        assertThat(prompt).doesNotContain("feedback:");
    }

    // --- trimUserAnswer, exercised through evaluateAnswer ---

    @Test
    void evaluateAnswer_shortUserAnswer_isKeptAsIsAndNotTruncated() {
        mockAiReply("score: 1\nfeedback: ok");

        openAiService.evaluateAnswer("Title", "Content", "Question?", "  Short answer  ");

        String prompt = capturePromptSentToAi();
        assertThat(prompt).contains("Short answer");
    }

    @Test
    void evaluateAnswer_longUserAnswer_isTruncatedAtMaxCharsWithoutMarker() {
        mockAiReply("score: 1\nfeedback: ok");
        String longAnswer = "a".repeat(1500);
        String expectedTruncated = longAnswer.substring(0, 1000);

        openAiService.evaluateAnswer("Title", "Content", "Question?", longAnswer);

        String prompt = capturePromptSentToAi();
        assertThat(prompt).contains(expectedTruncated);
        assertThat(prompt).doesNotContain("a".repeat(1001));
    }

    @Test
    void evaluateAnswer_nullUserAnswer_doesNotThrowAndIsTreatedAsEmpty() {
        mockAiReply("score: 1\nfeedback: ok");

        EvaluationResult result = openAiService.evaluateAnswer("Title", "Content", "Question?", null);

        assertThat(result.score()).isEqualTo(1.0);
        String prompt = capturePromptSentToAi();
        assertThat(prompt).doesNotContain("null");
    }

    // --- score security limits (parseEvaluation clamping), exercised through evaluateAnswer ---

    @Test
    void evaluateAnswer_scoreAboveOne_isClampedToOne() {
        mockAiReply("score: 5\nfeedback: way too generous");

        EvaluationResult result = openAiService.evaluateAnswer("Title", "Content", "Question?", "answer");

        assertThat(result.score()).isEqualTo(1.0);
    }

    @Test
    void evaluateAnswer_scoreBelowZero_isClampedToZero() {
        mockAiReply("score: -3\nfeedback: negative score");

        EvaluationResult result = openAiService.evaluateAnswer("Title", "Content", "Question?", "answer");

        assertThat(result.score()).isEqualTo(0.0);
    }

    @Test
    void evaluateAnswer_nonNumericScore_fallsBackToHalf() {
        mockAiReply("score: not-a-number\nfeedback: malformed");

        EvaluationResult result = openAiService.evaluateAnswer("Title", "Content", "Question?", "answer");

        assertThat(result.score()).isEqualTo(0.5);
    }

    @Test
    void evaluateAnswer_missingScoreLine_fallsBackToHalf() {
        mockAiReply("feedback: no score line at all");

        EvaluationResult result = openAiService.evaluateAnswer("Title", "Content", "Question?", "answer");

        assertThat(result.score()).isEqualTo(0.5);
    }

    @Test
    void evaluateAnswer_missingFeedbackLine_fallsBackToRawResponseText() {
        mockAiReply("score: 0.8\nno feedback prefix here");

        EvaluationResult result = openAiService.evaluateAnswer("Title", "Content", "Question?", "answer");

        assertThat(result.score()).isEqualTo(0.8);
        assertThat(result.feedback()).isEqualTo("score: 0.8\nno feedback prefix here");
    }

    @Test
    void evaluateAnswer_multipleScoreLines_lastOneWins() {
        mockAiReply("score: 0.2\nscore: 0.9\nfeedback: revised my mind");

        EvaluationResult result = openAiService.evaluateAnswer("Title", "Content", "Question?", "answer");

        assertThat(result.score()).isEqualTo(0.9);
    }

    @Test
    void evaluateAnswer_scoreAndFeedbackPrefixes_areCaseInsensitive() {
        mockAiReply("Score: 0.8\nFEEDBACK: good job");

        EvaluationResult result = openAiService.evaluateAnswer("Title", "Content", "Question?", "answer");

        assertThat(result.score()).isEqualTo(0.8);
        assertThat(result.feedback()).isEqualTo("good job");
    }

    @Test
    void evaluateAnswer_feedbackContainingColons_keepsOnlyFirstColonAsSeparator() {
        mockAiReply("score: 0.8\nfeedback: note: missed one edge case");

        EvaluationResult result = openAiService.evaluateAnswer("Title", "Content", "Question?", "answer");

        assertThat(result.feedback()).isEqualTo("note: missed one edge case");
    }
}

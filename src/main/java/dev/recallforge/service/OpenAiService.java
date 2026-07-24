package dev.recallforge.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import dev.recallforge.dto.AiResponse;

@Service
public class OpenAiService {
    
    private final RestClient openAiRestClient;
    private final String model;

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);

    // Below this, the user is still shaky on the topic and gets recall-level questions.
    private static final double PROFICIENCY_THRESHOLD = 0.7;

    public OpenAiService(RestClient openAiRestClient, @Value("${openai.model}") String model) {
        this.openAiRestClient = openAiRestClient;
        this.model = model;
    }

    public String generateQuestion(
            String topicTitle,
            String topicContent,
            double memoryScore,
            List<ReviewSummary> recentReviews) {
        String prompt = """
        Create one simple question to test understanding of this topic.

        Topic title:
        %s

        Topic content (a short paragraph, plus a code example whose comments
        are part of the explanation, not just syntax):
        %s

        User's proficiency on this topic: %s

        %sRules:
        - Return only the question.
        - Keep it short (one sentence when possible).
        - Test understanding of the concept, not memorization of code syntax.
        - Do not include code snippets, code blocks, or inline code in the question itself.
        - Do not ask about the output of code.
        - Prefer conceptual or practical questions that can be answered in a few sentences.
        - The question must be answerable using only facts, reasoning, or explanations
          that are explicitly stated above, in either the paragraph or the code's comments.
          Do not require outside knowledge or reasoning that isn't spelled out there.
        - Before asking "why" or "how", confirm the content above actually explains that
          reason or mechanism. If it doesn't, ask a "what" or "when" question instead.
        - Do not ask a question that duplicates or closely rephrases one of the
          recently asked questions listed above, if any.
        - Do not include the answer.
        """;
        prompt = prompt.formatted(
                topicTitle,
                trimTopicContent(topicContent),
                proficiencyGuidance(memoryScore),
                recentReviewsSection(recentReviews));

        AiResponse response = ask(prompt, 40);  // question generation
        return response.content();
    }

    private String proficiencyGuidance(double memoryScore) {
        if (memoryScore < PROFICIENCY_THRESHOLD) {
            return "EASY - the user is still building basic understanding of this topic. "
                + "Ask a foundational, recall-oriented question about a single concept "
                + "explicitly stated above.";
        }

        return "MEDIUM - the user already has a solid grasp of this topic. "
            + "Ask a question that requires connecting two ideas, explaining a "
            + "relationship, or applying the concept, not just recalling a definition.";
    }

    private String recentReviewsSection(List<ReviewSummary> recentReviews) {
        if (recentReviews == null || recentReviews.isEmpty()) {
            return "";
        }

        StringBuilder section = new StringBuilder(
            "Recently asked questions on this topic (avoid repeating these):\n");

        for (ReviewSummary review : recentReviews) {
            String feedbackSuffix = review.feedback() == null || review.feedback().isBlank()
                ? ""
                : ", feedback: " + trimText(review.feedback(), 200);

            section.append("- \"%s\" (score %.2f%s)\n".formatted(
                trimText(review.question(), 200),
                review.score(),
                feedbackSuffix));
        }
        section.append("\n");

        return section.toString();
    }

    public EvaluationResult evaluateAnswer(String topicTitle, String topicContent, String question, String userAnswer) {
        String prompt = """
        You are evaluating a learning review answer.

        Topic:
        %s

        Topic content:
        %s

        Question:
        %s

        User answer:
        %s

        Grade the answer from 0 to 1:
        - 1.0 = perfectly correct
        - 0.7 to 0.9 = mostly correct
        - 0.4 to 0.6 = partially correct
        - 0.1 to 0.3 = mostly incorrect but relevant
        - 0.0 = completely incorrect

        Ignore minor grammatical, spelling, or phrasing errors in the user's
        answer - grade and give feedback based on the correctness of the
        underlying understanding, not writing quality.

        Return exactly this format:

        score: <number>
        feedback: <short explanation>
        """;
        prompt = prompt.formatted(topicTitle, topicContent, question, trimUserAnswer(userAnswer));

        AiResponse response = ask(prompt, 80);  // answer evaluation
        return parseEvaluation(response.content(), response.totalTokens());
    }

    private AiResponse ask(String prompt, int maxTokens) {
        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", new Object[]{
                Map.of(
                    "role", "user", "content", prompt
                )
            },
            "temperature", 0.3,
            "max_tokens", maxTokens  // 100 tokens ≈ 75 words / gpt-4o-mini ~ 66k = $0.01
        );

        JsonNode response = openAiRestClient
                .post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("OpenAI returned an empty response.");
        }

        JsonNode contentNode = response
                .path("choices")
                .path(0)
                .path("message")
                .path("content");

        if (contentNode.isMissingNode()) {
            throw new IllegalStateException("Could not read OpenAI response: " + response);
        }

        JsonNode usage = response.path("usage");
        int promptTokens = usage.path("prompt_tokens").asInt();
        int completionTokens = usage.path("completion_tokens").asInt();
        int totalTokens = usage.path("total_tokens").asInt();

        String content = contentNode.asText().trim();
        
        log.info(
            "OpenAI tokens: prompt={}, completion={}, total={}",  // docker compose logs -f app | grep "OpenAI tokens"
            promptTokens,
            completionTokens,
            totalTokens
        );  

        return new AiResponse(
            content,
            promptTokens,
            completionTokens,
            totalTokens
        );
    }

    private EvaluationResult parseEvaluation(String text, int totalTokens) {
        double score = 0.5;
        String feedback = text;

        String[] lines = text.split("\\R");

        for (String line : lines) {
            String normalized = line.trim().toLowerCase();

            if (normalized.startsWith("score:")) {
                String value = line.substring(line.indexOf(":") + 1).trim();

                try {
                    score = Double.parseDouble(value);
                } catch (NumberFormatException ignored) {
                    score = 0.5;
                }
            }

            if (normalized.startsWith("feedback:")) {
                feedback = line.substring(line.indexOf(":") + 1).trim();
            }
        }

        score = Math.max(0.0, Math.min(1.0, score));

        return new EvaluationResult(score, feedback, totalTokens);
    }

    private String trimTopicContent(String content) {
        int maxChars = 3000;

        if (content == null) {
            return "";
        }

        String trimmed = content.trim();
        if (trimmed.length() <= maxChars) {
            return trimmed;
        }

        return trimmed.substring(0, maxChars) + "\n...[truncated]";
    }

    private String trimText(String text, int maxChars) {
        if (text == null) {
            return "";
        }

        String trimmed = text.trim();
        return trimmed.length() <= maxChars ? trimmed : trimmed.substring(0, maxChars);
    }

    private String trimUserAnswer(String answer) {
        int maxChars = 1000;

        if (answer == null) {
            return "";
        }

        String trimmed = answer.trim();
        if (trimmed.length() <= maxChars) {
            return trimmed;
        }

        return trimmed.substring(0, maxChars);
    }
}

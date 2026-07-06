package dev.recallforge.service;

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

    public OpenAiService(RestClient openAiRestClient, @Value("${openai.model}") String model) {
        this.openAiRestClient = openAiRestClient;
        this.model = model;
    }

    public String generateQuestion(String topicTitle, String topicContent) {
        String prompt = """
        Create one simple question to test this topic.

        Topic title:
        %s

        Topic content:
        %s

        Rules:
        - Return only the question.
        - Keep it short.
        - Do not include the answer.
        """;
        prompt = prompt.formatted(topicTitle, trimTopicContent(topicContent));

        AiResponse response = ask(prompt, 40);  // question generation
        return response.content();
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

        Return exactly this format:

        score: <number>
        feedback: <short explanation>
        """;
        prompt = prompt.formatted(topicTitle, topicContent, question, trimUserAnswer(userAnswer));

        AiResponse response = ask(prompt, 80);  // answer evaluation
        return parseEvaluation(response.content());
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

    private EvaluationResult parseEvaluation(String text) {
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

        return new EvaluationResult(score, feedback);
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

package dev.recallforge.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import dev.recallforge.dto.AiResponse;
import tools.jackson.databind.JsonNode;

@Service
public class GeminiProvider implements AiProvider {
    
    private final RestClient restClient;
    private final String model;

    private static final Logger log = 
        LoggerFactory.getLogger(AiService.class);

    public GeminiProvider(
            @Qualifier("geminiRestClient") RestClient restClient, 
            @Value("${gemini.model}") String model) {

        this.restClient = restClient;
        this.model = model;
    }

    @Override 
    public AiResponse ask(String prompt, int maxTokens) {

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of(
                    "role", "user",
                    "parts", List.of(
                        Map.of("text", prompt)
                    )
                )
            ),
            "generationConfig", Map.of(
                "maxOutputTokens", Math.max(maxTokens, 256),
                "temperature", 0.2,
                "thinkingConfig", Map.of(
                    "thinkingLevel", "minimal"
                )
            )
        );

        long start = System.nanoTime();

        JsonNode response = restClient
            .post()
            .uri("/models/{model}:generateContent", model)
            .body(requestBody)
            .retrieve()
            .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException(
                "Gemini returned an empty response."
            );
        }

        JsonNode candidate = response
            .path("candidates")
            .path(0);

        JsonNode parts = candidate
            .path("content")
            .path("parts");

        StringBuilder text = new StringBuilder();

        if (parts.isArray()) {
            for (JsonNode part : parts) {
                JsonNode textNode = part.path("text");

                if (!textNode.isMissingNode()
                        && !textNode.isNull()) {
                    text.append(textNode.asString());
                }
            }
        }

        String content = text.toString().trim();

        if (content.isEmpty()) {
            throw new IllegalStateException(
                "Gemini returned no text. Finish reason: "
                + candidate.path("finishReason").asString()
                + ", response: " + response
            );
        }

        JsonNode usage = response.path("usageMetadata");

        int promptTokens = usage.path("promptTokenCount").asInt(0);
        int completionTokens = usage.path("candidatesTokenCount").asInt(0);
        int reasoningTokens = usage.path("thoughtsTokenCount").asInt(0);
        int totalTokens = usage.path("totalTokenCount").asInt(0);
        long latencyMs = (System.nanoTime() - start) / 1_000_000;

        log.info(
            "Gemini tokens: model={}, latency={}ms, prompt={}, completion={}, reasoning={}, total={}",
            model,
            latencyMs,
            promptTokens,
            completionTokens,
            reasoningTokens,
            totalTokens
        );

        //log.debug("Gemini prompt: {}", prompt);  // docker compose logs -f app | grep "Gemini tokens"

        return new AiResponse(
            content,
            promptTokens,
            completionTokens,
            totalTokens
        );
    }
}

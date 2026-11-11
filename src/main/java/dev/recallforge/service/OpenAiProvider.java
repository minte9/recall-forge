package dev.recallforge.service;

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
public class OpenAiProvider implements AiProvider {
    
    private final RestClient restClient;
    private final String model;

    private static final Logger log = 
        LoggerFactory.getLogger(AiService.class);

    public OpenAiProvider(
            @Qualifier("openAiRestClient") RestClient restClient, 
            @Value("${openai.model}") String model) {

        this.restClient = restClient;
        this.model = model;
    }

    @Override 
    public AiResponse ask(String prompt, int maxTokens) {

        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", new Object[]{
                Map.of(
                    "role", "user",
                    "content", prompt
                )
            },
            "reasoning_effort", "none",
            "max_completion_tokens", maxTokens
        );

        long start = System.nanoTime();

        JsonNode response = restClient
                .post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException(
                "OpenAI returned an empty response."
            );
        }

        JsonNode choice = response
                .path("choices")
                .path(0);

        JsonNode contentNode = choice
                .path("message")
                .path("content");

        String content = contentNode.asString().trim();

        if (content.isEmpty()) {
            throw new IllegalStateException(
                "OpenAI returned no text. Finish reason: "
                + choice.path("finish_reason").asString()
                + ", response: " + response
            );
        }

        JsonNode usage = response.path("usage");

        int promptTokens = usage.path("prompt_tokens").asInt();
        int completionTokens = usage.path("completion_tokens").asInt();
        int totalTokens = usage.path("total_tokens").asInt();
        int reasoningTokens = usage
            .path("completion_tokens_details")
            .path("reasoning_tokens")
            .asInt(0);
        long latencyMs = (System.nanoTime() - start) / 1_000_000;

        log.info(
            "OpenAI tokens: model={}, latency={}ms, prompt={}, completion={}, reasoning={}, total={}",
            model,
            latencyMs,
            promptTokens,
            completionTokens,
            reasoningTokens,
            totalTokens
        );

        //log.debug("OpenAI prompt: {}", prompt);  // docker compose logs -f app | grep "OpenAI tokens"

        return new AiResponse(
            content,
            promptTokens,
            completionTokens,
            totalTokens
        );
    }
}

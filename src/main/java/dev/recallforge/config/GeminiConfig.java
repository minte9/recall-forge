package dev.recallforge.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration 
public class GeminiConfig {

    // Gemini calls aren't streamed, so nothing arrives until the whole answer
    // is ready - a long Optimize rewrite can stay silent well past 10 seconds.
    static final Duration READ_TIMEOUT = Duration.ofSeconds(120);
    
    @Bean
    public RestClient geminiRestClient(
            @Value("${gemini.api-key}") String apiKey) {

        return RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta")
            .requestFactory(requestFactory())
            .defaultHeader("x-goog-api-key", apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    static ClientHttpRequestFactory requestFactory() {
        ReactorClientHttpRequestFactory factory = new ReactorClientHttpRequestFactory();
        factory.setReadTimeout(READ_TIMEOUT);
        return factory;
    }

}

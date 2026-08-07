package dev.recallforge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AppAiProperties (
    boolean enabled
) {
}

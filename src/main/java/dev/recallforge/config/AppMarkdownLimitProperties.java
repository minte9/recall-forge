package dev.recallforge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.markdown")
public record AppMarkdownLimitProperties(
    int maxTopicsPerUser
) {
}

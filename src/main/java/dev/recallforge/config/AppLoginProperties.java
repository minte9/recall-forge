package dev.recallforge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.login")
public record AppLoginProperties(
    boolean enabled
) {}

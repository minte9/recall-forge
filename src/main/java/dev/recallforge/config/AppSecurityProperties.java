package dev.recallforge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties (
    boolean enabled,
    String username,
    String password
) {
}
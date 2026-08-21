package dev.recallforge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.goal-days")
public record AppGoalProperties(
    int reviews,
    int codeChallenges
) {
}

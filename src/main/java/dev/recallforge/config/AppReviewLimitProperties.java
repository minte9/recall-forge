package dev.recallforge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.reviews")
public record AppReviewLimitProperties(
    int dailyLimit,
    int dailyQuestionLimit
) {
}

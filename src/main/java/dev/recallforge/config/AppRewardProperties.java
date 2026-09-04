package dev.recallforge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rewards")
public record AppRewardProperties(
    int codeChallengeXp
) {    
}

package dev.recallforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import dev.recallforge.config.AppAiProperties;
import dev.recallforge.config.AppGoalProperties;
import dev.recallforge.config.AppMarkdownLimitProperties;
import dev.recallforge.config.AppReviewLimitProperties;
import dev.recallforge.config.AppRewardProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    AppReviewLimitProperties.class,
    AppAiProperties.class,
    AppGoalProperties.class,
    AppRewardProperties.class,
    AppMarkdownLimitProperties.class
})
public class RecallForgeApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RecallForgeApplication.class, args);
    }
}

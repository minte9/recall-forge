package dev.recallforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import dev.recallforge.config.AppAiProperties;
import dev.recallforge.config.AppGoalProperties;
import dev.recallforge.config.AppReviewLimitProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    AppReviewLimitProperties.class,
    AppAiProperties.class,
    AppGoalProperties.class
})
public class RecallForgeApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RecallForgeApplication.class, args);
    }
}

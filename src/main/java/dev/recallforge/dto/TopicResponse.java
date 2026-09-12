package dev.recallforge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import dev.recallforge.domain.Topic;

public record TopicResponse(
        Long id, 
        String title, 
        double memoryScore,
        @JsonFormat(
            shape = JsonFormat.Shape.STRING, 
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", 
            timezone = "UTC")
        LocalDateTime nextReviewAt, 
        long codeChallengeXp,
        int reviewStreak) {
    
    public static TopicResponse from(Topic topic) {
        return from(topic, 0L);
    }

    public static TopicResponse from(Topic topic, long codeChallengeXp) {
        return new TopicResponse(
            topic.getId(),
            topic.getTitle(),
            topic.getMemoryScore(),
            topic.getNextReviewAt(),
            codeChallengeXp,
            topic.getReviewStreak()
        );
    }
}

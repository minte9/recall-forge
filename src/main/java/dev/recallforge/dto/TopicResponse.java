package dev.recallforge.dto;

import java.time.LocalDateTime;
import dev.recallforge.domain.Topic;

public record TopicResponse(
        Long id, String title, double memoryScore, LocalDateTime nextReviewAt) {
            
    public static TopicResponse from(Topic topic) {
        return new TopicResponse(
            topic.getId(),
            topic.getTitle(),
            topic.getMemoryScore(),
            topic.getNextReviewAt()
        );
    }
}

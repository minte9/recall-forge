package dev.recallforge.dto;

import java.time.LocalDateTime;

public record AnswerResponse(
    Long topicId,
    String topicTitle,
    double score,
    String feedback,
    double updateMemoryScore,
    LocalDateTime nextReviewAt
) {
}

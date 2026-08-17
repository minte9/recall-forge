package dev.recallforge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record AnswerResponse(
    Long topicId,
    String topicTitle,
    double score,
    String feedback,
    double updateMemoryScore,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime nextReviewAt,
    int reviewStreak,
    RewardResponse reward,
    long dueInSubcategory,
    String subcategory
) {
}

package dev.recallforge.assistant;

import java.time.LocalDateTime;

public record MistakeDto(
    String category,
    String subcategory,
    String topicTitle,
    String question,
    String userAnswer,
    String feedback,
    double score,
    LocalDateTime reviewedAt
) {}
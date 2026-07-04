package dev.recallforge.dto;

import java.time.LocalDateTime;

public record FeedbackHistoryResponse(
    Long id,
    String question, 
    String userAnswer,
    String feedback,
    LocalDateTime createdAt,
    Double score
) {}
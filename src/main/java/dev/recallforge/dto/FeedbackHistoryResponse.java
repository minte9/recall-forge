package dev.recallforge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record FeedbackHistoryResponse(
    Long id,
    String question,
    String userAnswer,
    String feedback,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime createdAt,
    Double score
) {}
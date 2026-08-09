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
    Double score,
    // True for a Try Again / Code Challenge practice attempt - lets the
    // frontend flag it in the summary and exclude it from the average score.
    boolean isRetry
) {}
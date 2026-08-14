package dev.recallforge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

import dev.recallforge.domain.RetryType;

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
    boolean isRetry,
    // Null for retries saved before this field existed - the frontend shows
    // the plain "Practice" badge with no R/C letter in that case.
    RetryType retryType
) {}
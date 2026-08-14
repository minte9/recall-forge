package dev.recallforge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record CodeChallengeResponse(
    Long topicId,
    String topicTitle,
    String question,
    String userAnswer,
    String feedback,
    Double score,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime reviewedAt
) {}

package dev.recallforge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import dev.recallforge.domain.Review;

import java.time.LocalDateTime;

public record ReviewHistoryResponse(
        Long id,
        Long topicId,
        String topicTitle,
        String question,
        String userAnswer,
        double score,
        String feedback,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        LocalDateTime reviewedAt
) {
    public static ReviewHistoryResponse from(Review review) {
        return new ReviewHistoryResponse(
                review.getId(),
                review.getTopic().getId(),
                review.getTopic().getTitle(),
                review.getQuestion(),
                review.getUserAnswer(),
                review.getScore(),
                review.getFeedback(),
                review.getReviewedAt()
        );
    }
}
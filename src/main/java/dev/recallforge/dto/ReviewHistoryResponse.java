package dev.recallforge.dto;

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
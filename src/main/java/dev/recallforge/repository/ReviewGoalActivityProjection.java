package dev.recallforge.repository;

import java.time.LocalDateTime;

import dev.recallforge.domain.RetryType;

public interface ReviewGoalActivityProjection {
    LocalDateTime getReviewedAt();
    double getScore();
    RetryType getRetryType();
}

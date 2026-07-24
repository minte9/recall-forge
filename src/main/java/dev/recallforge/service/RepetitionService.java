package dev.recallforge.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

/**
 * Pure spaced-repetition math: no persistence, no user context.
 *
 * Used by {@link ReviewService} after grading an answer to blend the new
 * score into a topic's memory score and to pick the next review date from
 * that score. Kept separate from {@link ReviewService} so the scoring rules
 * are easy to find and adjust without touching orchestration logic.
 */
@Service
public class RepetitionService {

    /**
     * Memory score update:
     * 
     * We blend old memory score with the new review score. 
     * This avoids one bad answer destroying the full memory state. 
     * But the recent performance should matter more (good for dashboard mastery).
     */
    public double calculateUpdateMemoryScore(double oldScore, double newScore) {
        //return oldScore * 0.7 + newScore * 0.3;
        return oldScore * 0.3 + newScore * 0.7;
    }

    /**
     * Spaced repetition rule:
     * 
     * Weak answers come back soon.
     * Strong answers come back later
     */
    public LocalDateTime calculateNextReviewAt(double score) {
        int intervalDays;

        if (score < 0.4) {
            intervalDays = 1;
        } else
        if (score < 0.7) {
            intervalDays = 3;
        } else
        if (score < 0.9) {
            intervalDays = 7;
        } else {
            intervalDays = 14;
        }

        return LocalDateTime.now().plusDays(intervalDays);
    }

    /**
     * Skipping a question isn't an answer, so it shouldn't touch memoryScore.
     * Push it out by the shortest existing interval so it drops out of
     * today's due queue without implying anything about answer quality.
     */
    public LocalDateTime calculateSkipReviewAt() {
        return LocalDateTime.now().plusDays(1);
    }
}

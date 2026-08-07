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

    // Scores at or above this count as a "success" that extends the streak;
    // anything below resets it. Matches OpenAiService's own proficiency cutoff.
    private static final double STREAK_SUCCESS_THRESHOLD = 0.7;

    // Interval growth per consecutive success once a streak is underway
    // (SM-2/Anki-style ease factor), and the ceiling that keeps growth from
    // running away once a topic is clearly mastered.
    private static final double EASE_FACTOR = 1.8;
    private static final long MAX_INTERVAL_DAYS = 180;

    public record RepetitionResult(LocalDateTime nextReviewAt, int reviewStreak) {
    }

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
     * Weak answers come back soon. Strong answers come back later, and
     * further out still the more consecutive strong answers in a row -
     * a topic that's clearly mastered should stop coming back every
     * couple of weeks forever.
     */
    public RepetitionResult calculateNextReview(double score, int previousStreak) {
        int baseIntervalDays;

        if (score < 0.4) {
            baseIntervalDays = 1;
        } else
        if (score < 0.7) {
            baseIntervalDays = 3;
        } else
        if (score < 0.9) {
            baseIntervalDays = 7;
        } else {
            baseIntervalDays = 14;
        }

        int newStreak = score >= STREAK_SUCCESS_THRESHOLD ? previousStreak + 1 : 0;

        long intervalDays = baseIntervalDays;
        if (newStreak >= 2) {
            double multiplier = Math.pow(EASE_FACTOR, newStreak - 1);
            intervalDays = Math.min(MAX_INTERVAL_DAYS, Math.round(baseIntervalDays * multiplier));
        }

        return new RepetitionResult(LocalDateTime.now().plusDays(intervalDays), newStreak);
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

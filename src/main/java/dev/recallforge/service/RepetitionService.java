package dev.recallforge.service;

import java.time.LocalDate;
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

    // Scores at or above this count as a "success" that moves the topic up
    // one box (streak + 1); anything below moves it down one box. Matches
    // OpenAiService's own proficiency cutoff, and the frontend's
    // resultBoxAdvanced in app.js - keep them in sync.
    private static final double STREAK_SUCCESS_THRESHOLD = 0.7;

    // Interval growth per box once a topic has moved up a few boxes
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
     * further out still the higher the topic's box - a topic that's
     * clearly mastered should stop coming back every couple of weeks
     * forever. Due dates land at midnight, so a day's reviews all come
     * due together.
     *
     * The streak works like a Leitner box (shown to the user as box
     * streak + 1): a strong answer moves the topic up one box, a weaker
     * one moves it down one box instead of resetting it, since a single
     * slip after a run of good answers is usually a varied question or a
     * harsh grade rather than real forgetting. Growth only applies after
     * a strong answer, so a weaker one always comes back within 1-3 days
     * whatever the box.
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

        boolean success = score >= STREAK_SUCCESS_THRESHOLD;
        int newStreak = success ? previousStreak + 1 : Math.max(0, previousStreak - 1);

        long intervalDays = baseIntervalDays;
        if (success && newStreak >= 2) {
            double multiplier = Math.pow(EASE_FACTOR, newStreak - 1);
            intervalDays = Math.min(MAX_INTERVAL_DAYS, Math.round(baseIntervalDays * multiplier));
        }

        return new RepetitionResult(LocalDate.now().plusDays(intervalDays).atStartOfDay(), newStreak);
    }

    /**
     * Skipping a question isn't an answer, so it shouldn't touch memoryScore.
     * Push it out by the shortest existing interval so it drops out of
     * today's due queue without implying anything about answer quality.
     * Due dates land at midnight, so a day's reviews all come due together.
     */
    public LocalDateTime calculateSkipReviewAt() {
        return LocalDate.now().plusDays(1).atStartOfDay();
    }
}

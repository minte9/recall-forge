package dev.recallforge.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class RepetitionService {
    
    /**
     * Memory score update:
     * 
     * We blend old memory score with the new review score. 
     * This avoids one bad answer destroying the full memory state.
     */
    public double calculateUpdateMemoryScore(double oldScore, double newScore) {
        return oldScore * 0.7 + newScore * 0.3;
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
}

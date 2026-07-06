package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class RepetitionServiceTest {

    private final RepetitionService repetitionService = new RepetitionService();

    @Test
    void calculateUpdateMemoryScore_blendsOldAndNewScore() {
        double result = repetitionService.calculateUpdateMemoryScore(0.8, 0.2);

        assertThat(result).isCloseTo(0.8 * 0.3 + 0.2 * 0.7, within(1e-9));
    }

    @Test
    void calculateNextReviewAt_scheduleBelow0_4In1Day() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime result = repetitionService.calculateNextReviewAt(0.39);
        LocalDateTime after = LocalDateTime.now();

        assertThat(result).isBetween(before.plusDays(1), after.plusDays(1));
    }

    @Test
    void calculateNextReviewAt_scheduleBelow0_7In3Days() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime result = repetitionService.calculateNextReviewAt(0.69);
        LocalDateTime after = LocalDateTime.now();

        assertThat(result).isBetween(before.plusDays(3), after.plusDays(3));
    }

    @Test
    void calculateNextReviewAt_scheduleBelow0_9In7Days() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime result = repetitionService.calculateNextReviewAt(0.89);
        LocalDateTime after = LocalDateTime.now();

        assertThat(result).isBetween(before.plusDays(7), after.plusDays(7));
    }

    @Test
    void calculateNextReviewAt_scheduleAtOrAbove0_9In14Days() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime result = repetitionService.calculateNextReviewAt(0.9);
        LocalDateTime after = LocalDateTime.now();

        assertThat(result).isBetween(before.plusDays(14), after.plusDays(14));
    }
}

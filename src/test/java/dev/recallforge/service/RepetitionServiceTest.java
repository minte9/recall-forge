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
    void calculateNextReview_scheduleBelow0_4In1DayAndResetsStreak() {
        LocalDateTime before = LocalDateTime.now();
        RepetitionService.RepetitionResult result = repetitionService.calculateNextReview(0.39, 3);
        LocalDateTime after = LocalDateTime.now();

        assertThat(result.nextReviewAt()).isBetween(before.plusDays(1), after.plusDays(1));
        assertThat(result.reviewStreak()).isZero();
    }

    @Test
    void calculateNextReview_scheduleBelow0_7In3DaysAndResetsStreak() {
        LocalDateTime before = LocalDateTime.now();
        RepetitionService.RepetitionResult result = repetitionService.calculateNextReview(0.69, 3);
        LocalDateTime after = LocalDateTime.now();

        assertThat(result.nextReviewAt()).isBetween(before.plusDays(3), after.plusDays(3));
        assertThat(result.reviewStreak()).isZero();
    }

    @Test
    void calculateNextReview_firstSuccessScheduleBelow0_9In7DaysWithStreakOne() {
        LocalDateTime before = LocalDateTime.now();
        RepetitionService.RepetitionResult result = repetitionService.calculateNextReview(0.89, 0);
        LocalDateTime after = LocalDateTime.now();

        assertThat(result.nextReviewAt()).isBetween(before.plusDays(7), after.plusDays(7));
        assertThat(result.reviewStreak()).isEqualTo(1);
    }

    @Test
    void calculateNextReview_firstSuccessAtOrAbove0_9In14DaysWithStreakOne() {
        LocalDateTime before = LocalDateTime.now();
        RepetitionService.RepetitionResult result = repetitionService.calculateNextReview(0.9, 0);
        LocalDateTime after = LocalDateTime.now();

        assertThat(result.nextReviewAt()).isBetween(before.plusDays(14), after.plusDays(14));
        assertThat(result.reviewStreak()).isEqualTo(1);
    }

    @Test
    void calculateNextReview_secondConsecutiveSuccessGrowsIntervalByEaseFactor() {
        LocalDateTime before = LocalDateTime.now();
        RepetitionService.RepetitionResult result = repetitionService.calculateNextReview(0.9, 1);
        LocalDateTime after = LocalDateTime.now();

        // base 14 days * 1.8^1 = 25.2 -> rounds to 25
        assertThat(result.nextReviewAt()).isBetween(before.plusDays(25), after.plusDays(25));
        assertThat(result.reviewStreak()).isEqualTo(2);
    }

    @Test
    void calculateNextReview_longStreakCapsAtMaxIntervalDays() {
        LocalDateTime before = LocalDateTime.now();
        RepetitionService.RepetitionResult result = repetitionService.calculateNextReview(0.9, 10);
        LocalDateTime after = LocalDateTime.now();

        assertThat(result.nextReviewAt()).isBetween(before.plusDays(180), after.plusDays(180));
        assertThat(result.reviewStreak()).isEqualTo(11);
    }

    @Test
    void calculateSkipReviewAt_schedules1DayOut() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime result = repetitionService.calculateSkipReviewAt();
        LocalDateTime after = LocalDateTime.now();

        assertThat(result).isBetween(before.plusDays(1), after.plusDays(1));
    }
}

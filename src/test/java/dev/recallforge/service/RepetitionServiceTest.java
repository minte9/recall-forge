package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDate;
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
    void calculateNextReview_scheduleBelow0_4In1DayAndMovesDownOneBox() {
        RepetitionService.RepetitionResult result = repetitionService.calculateNextReview(0.39, 3);

        assertThat(result.nextReviewAt()).isEqualTo(LocalDate.now().plusDays(1).atStartOfDay());
        assertThat(result.reviewStreak()).isEqualTo(2);
    }

    @Test
    void calculateNextReview_scheduleBelow0_7In3DaysAndMovesDownOneBox() {
        // streak 2 would grow a success's interval, but a weak answer
        // always comes back at its base interval
        RepetitionService.RepetitionResult result = repetitionService.calculateNextReview(0.69, 3);

        assertThat(result.nextReviewAt()).isEqualTo(LocalDate.now().plusDays(3).atStartOfDay());
        assertThat(result.reviewStreak()).isEqualTo(2);
    }

    @Test
    void calculateNextReview_weakAnswerInFirstBoxStaysThere() {
        RepetitionService.RepetitionResult result = repetitionService.calculateNextReview(0.2, 0);

        assertThat(result.nextReviewAt()).isEqualTo(LocalDate.now().plusDays(1).atStartOfDay());
        assertThat(result.reviewStreak()).isZero();
    }

    @Test
    void calculateNextReview_firstSuccessScheduleBelow0_9In7DaysWithStreakOne() {
        RepetitionService.RepetitionResult result = repetitionService.calculateNextReview(0.89, 0);

        assertThat(result.nextReviewAt()).isEqualTo(LocalDate.now().plusDays(7).atStartOfDay());
        assertThat(result.reviewStreak()).isEqualTo(1);
    }

    @Test
    void calculateNextReview_firstSuccessAtOrAbove0_9In14DaysWithStreakOne() {
        RepetitionService.RepetitionResult result = repetitionService.calculateNextReview(0.9, 0);

        assertThat(result.nextReviewAt()).isEqualTo(LocalDate.now().plusDays(14).atStartOfDay());
        assertThat(result.reviewStreak()).isEqualTo(1);
    }

    @Test
    void calculateNextReview_secondConsecutiveSuccessGrowsIntervalByEaseFactor() {
        RepetitionService.RepetitionResult result = repetitionService.calculateNextReview(0.9, 1);

        // base 14 days * 1.8^1 = 25.2 -> rounds to 25
        assertThat(result.nextReviewAt()).isEqualTo(LocalDate.now().plusDays(25).atStartOfDay());
        assertThat(result.reviewStreak()).isEqualTo(2);
    }

    @Test
    void calculateNextReview_longStreakCapsAtMaxIntervalDays() {
        RepetitionService.RepetitionResult result = repetitionService.calculateNextReview(0.9, 10);

        assertThat(result.nextReviewAt()).isEqualTo(LocalDate.now().plusDays(180).atStartOfDay());
        assertThat(result.reviewStreak()).isEqualTo(11);
    }

    @Test
    void calculateSkipReviewAt_schedulesMidnight1DayOut() {
        LocalDateTime result = repetitionService.calculateSkipReviewAt();

        assertThat(result).isEqualTo(LocalDate.now().plusDays(1).atStartOfDay());
    }
}

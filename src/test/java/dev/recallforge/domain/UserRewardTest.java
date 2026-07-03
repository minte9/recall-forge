package dev.recallforge.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class UserRewardTest {

    @Test
    void currentStreakDays_isZero_whenNoReviewHasEverHappened() {
        UserReward reward = new UserReward(1L);

        assertThat(reward.currentStreakDays(LocalDate.now())).isZero();
    }

    @Test
    void currentStreakDays_returnsStreak_whenLastReviewWasToday() {
        UserReward reward = new UserReward(1L);
        LocalDate today = LocalDate.now();

        reward.addReviewReward(10, false, today);

        assertThat(reward.currentStreakDays(today)).isEqualTo(1);
    }

    @Test
    void currentStreakDays_returnsStreak_whenLastReviewWasYesterday() {
        UserReward reward = new UserReward(1L);
        LocalDate today = LocalDate.now();

        reward.addReviewReward(10, false, today.minusDays(1));

        assertThat(reward.currentStreakDays(today)).isEqualTo(1);
    }

    @Test
    void currentStreakDays_isZero_whenLastReviewWasMoreThanOneDayAgo() {
        UserReward reward = new UserReward(1L);
        LocalDate today = LocalDate.now();

        reward.addReviewReward(10, false, today.minusDays(3));

        assertThat(reward.currentStreakDays(today)).isZero();
        assertThat(reward.getStreakDays()).isEqualTo(1);
    }
}

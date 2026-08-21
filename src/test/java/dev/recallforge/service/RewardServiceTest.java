package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.recallforge.config.AppGoalProperties;
import dev.recallforge.domain.RetryType;
import dev.recallforge.domain.UserReward;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.repository.ReviewGoalActivityProjection;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.repository.UserRewardRepository;
import dev.recallforge.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private UserRewardRepository userRewardRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private ReviewRepository reviewRepository;

    private RewardService rewardService;

    private void setUp() {
        rewardService = new RewardService(
            userRewardRepository, currentUserService, topicRepository, reviewRepository,
            new AppGoalProperties(10, 3)
        );
    }

    @Test
    void rewardDefaultUser_createsRewardOnFirstReview_andStartsStreakAtOne() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRewardRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of(LocalDateTime.now()));

        RewardResponse response = rewardService.rewardDefaultUser(0.2, "java");

        assertThat(response.streakDays()).isEqualTo(1);
    }

    @Test
    void getCurrentUserReward_doesNotCreateOrMutateRewardWhenOneAlreadyExists() {
        setUp();
        UserReward existingReward = new UserReward(1L);
        existingReward.addReviewReward(false, LocalDate.now());

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(existingReward));

        rewardService.getCurrentUserReward("java");

        verify(userRewardRepository, never()).save(any());
    }

    @Test
    void getCurrentUserReward_streakReadsZero_whenUserMissedMoreThanOneDay() {
        setUp();
        UserReward existingReward = new UserReward(1L);
        existingReward.addReviewReward(false, LocalDate.now().minusDays(3));

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(existingReward));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of(LocalDateTime.now().minusDays(3)));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.streakDays()).isZero();
    }

    @Test
    void getCurrentUserReward_streakStillCounts_whenLastReviewWasYesterday() {
        setUp();
        UserReward existingReward = new UserReward(1L);
        existingReward.addReviewReward(false, LocalDate.now().minusDays(1));

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(existingReward));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of(LocalDateTime.now().minusDays(1)));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.streakDays()).isEqualTo(1);
    }

    @Test
    void getCurrentUserReward_goalDaysCountsADayWithTenHighScoreReviews() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of());
        when(reviewRepository.findGoalActivityByUserIdAndCategorySince(eq(1L), eq("java"), any()))
            .thenReturn(activityOnOneDay(10, 0.8, RetryType.REVIEW));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.goalDaysThisMonth()).isEqualTo(1);
    }

    @Test
    void getCurrentUserReward_goalDaysExcludesDayWithFewerThanTenHighScoreReviews() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of());
        when(reviewRepository.findGoalActivityByUserIdAndCategorySince(eq(1L), eq("java"), any()))
            .thenReturn(activityOnOneDay(9, 0.8, RetryType.REVIEW));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.goalDaysThisMonth()).isZero();
    }

    @Test
    void getCurrentUserReward_goalDaysIgnoresLowScoringReviews() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of());
        when(reviewRepository.findGoalActivityByUserIdAndCategorySince(eq(1L), eq("java"), any()))
            .thenReturn(activityOnOneDay(10, 0.5, RetryType.REVIEW));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.goalDaysThisMonth()).isZero();
    }

    @Test
    void getCurrentUserReward_goalDaysCountsADayWithThreeCodeChallenges() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of());
        when(reviewRepository.findGoalActivityByUserIdAndCategorySince(eq(1L), eq("java"), any()))
            .thenReturn(activityOnOneDay(3, 0.3, RetryType.CODE_CHALLENGE));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.goalDaysThisMonth()).isEqualTo(1);
    }

    @Test
    void getCurrentUserReward_goalReachedTodayIsTrue_whenTodayHitsTheReviewThreshold() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of());
        when(reviewRepository.findGoalActivityByUserIdAndCategorySince(eq(1L), eq("java"), any()))
            .thenReturn(activityOnDay(LocalDateTime.now(), 10, 0.8, RetryType.REVIEW));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.goalReachedToday()).isTrue();
    }

    @Test
    void getCurrentUserReward_goalReachedTodayIsTrue_whenTodayHitsTheCodeChallengeThreshold() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of());
        when(reviewRepository.findGoalActivityByUserIdAndCategorySince(eq(1L), eq("java"), any()))
            .thenReturn(activityOnDay(LocalDateTime.now(), 3, 0.3, RetryType.CODE_CHALLENGE));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.goalReachedToday()).isTrue();
    }

    @Test
    void getCurrentUserReward_goalReachedTodayIsFalse_whenOnlyAnEarlierDayQualified() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of());
        when(reviewRepository.findGoalActivityByUserIdAndCategorySince(eq(1L), eq("java"), any()))
            .thenReturn(activityOnDay(LocalDateTime.now().minusDays(3), 10, 0.8, RetryType.REVIEW));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.goalDaysThisMonth()).isEqualTo(1);
        assertThat(response.goalReachedToday()).isFalse();
    }

    @Test
    void getCurrentUserReward_goalDaysCountsEachQualifyingDayInTheMonth() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of());

        List<ReviewGoalActivityProjection> activity = new ArrayList<>();
        activity.addAll(activityOnDay(LocalDateTime.now().minusDays(3), 10, 0.8, RetryType.REVIEW));
        activity.addAll(activityOnDay(LocalDateTime.now(), 10, 0.8, RetryType.REVIEW));

        when(reviewRepository.findGoalActivityByUserIdAndCategorySince(eq(1L), eq("java"), any())).thenReturn(activity);

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.goalDaysThisMonth()).isEqualTo(2);
    }

    @Test
    void getCurrentUserReward_goalDaysQueriesActivitySinceStartOfCurrentMonth() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of());
        when(reviewRepository.findGoalActivityByUserIdAndCategorySince(eq(1L), eq("java"), any()))
            .thenReturn(List.of());

        rewardService.getCurrentUserReward("java");

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        verify(reviewRepository).findGoalActivityByUserIdAndCategorySince(1L, "java", startOfMonth);
    }

    private List<ReviewGoalActivityProjection> activityOnOneDay(int count, double score, RetryType retryType) {
        return activityOnDay(LocalDateTime.of(2026, 1, 5, 9, 0), count, score, retryType);
    }

    private List<ReviewGoalActivityProjection> activityOnDay(
        LocalDateTime day, int count, double score, RetryType retryType
    ) {
        List<ReviewGoalActivityProjection> activity = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            activity.add(new TestActivity(day.plusMinutes(i), score, retryType));
        }

        return activity;
    }

    private record TestActivity(
        LocalDateTime reviewedAtValue,
        double scoreValue,
        RetryType retryTypeValue
    ) implements ReviewGoalActivityProjection {

        @Override
        public LocalDateTime getReviewedAt() {
            return reviewedAtValue;
        }

        @Override
        public double getScore() {
            return scoreValue;
        }

        @Override
        public RetryType getRetryType() {
            return retryTypeValue;
        }
    }
}

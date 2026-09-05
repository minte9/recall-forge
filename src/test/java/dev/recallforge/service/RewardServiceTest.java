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
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.recallforge.config.AppGoalProperties;
import dev.recallforge.config.AppRewardProperties;
import dev.recallforge.domain.RetryType;
import dev.recallforge.domain.UserReward;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.repository.MarkdownFileCodeChallengeCountProjection;
import dev.recallforge.repository.ReviewGoalActivityProjection;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.TopicCodeChallengeCountProjection;
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

    private static final int CODE_CHALLENGE_XP = 25;

    private void setUp() {
        rewardService = new RewardService(
            userRewardRepository, currentUserService, topicRepository, reviewRepository,
            new AppGoalProperties(10, 3),
            new AppRewardProperties(CODE_CHALLENGE_XP)
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

        RewardResponse response = rewardService.rewardDefaultUser(0.2, "java", RetryType.REVIEW, false);

        assertThat(response.streakDays()).isEqualTo(1);
    }

    // XP is computed live per category: 25 * (perfect code challenges in that
    // category). rewardDefaultUser still reports lastXpAwarded for the answer
    // just graded, and leveledUp when that award crossed a level boundary.
    @Test
    void rewardDefaultUser_perfectCodeChallenge_awardsCodeChallengeXp() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        // the challenge just graded is already persisted, so the live count sees it
        when(reviewRepository.countPerfectCodeChallengesByUserIdAndCategory(1L, "java", 1.0))
            .thenReturn(1L);

        RewardResponse response =
            rewardService.rewardDefaultUser(1.0, "java", RetryType.CODE_CHALLENGE, false);

        assertThat(response.lastXpAwarded()).isEqualTo(CODE_CHALLENGE_XP);
        assertThat(response.xp()).isEqualTo(CODE_CHALLENGE_XP);
    }

    // A "Try Again" resubmission of an already-perfected challenge must not
    // add XP, even if it also scores a perfect 1.0 - otherwise repeatedly
    // retrying one challenge could be farmed for unlimited XP.
    @Test
    void rewardDefaultUser_retryOfPerfectCodeChallenge_awardsNoXp() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(reviewRepository.countPerfectCodeChallengesByUserIdAndCategory(1L, "java", 1.0))
            .thenReturn(1L);

        RewardResponse response =
            rewardService.rewardDefaultUser(1.0, "java", RetryType.CODE_CHALLENGE, true);

        assertThat(response.lastXpAwarded()).isZero();
    }

    @Test
    void rewardDefaultUser_codeChallengeBelowPerfectScore_awardsNoXp() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(reviewRepository.countPerfectCodeChallengesByUserIdAndCategory(1L, "java", 1.0))
            .thenReturn(0L);

        RewardResponse response =
            rewardService.rewardDefaultUser(0.8, "java", RetryType.CODE_CHALLENGE, false);

        assertThat(response.lastXpAwarded()).isZero();
        assertThat(response.xp()).isZero();
    }

    @Test
    void rewardDefaultUser_perfectPlainReview_awardsNoXp() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(reviewRepository.countPerfectCodeChallengesByUserIdAndCategory(1L, "java", 1.0))
            .thenReturn(2L);

        RewardResponse response =
            rewardService.rewardDefaultUser(1.0, "java", RetryType.REVIEW, false);

        // XP still reflects the 2 earlier code challenges, but this review added nothing
        assertThat(response.xp()).isEqualTo(2 * CODE_CHALLENGE_XP);
        assertThat(response.lastXpAwarded()).isZero();
    }

    @Test
    void rewardDefaultUser_reportsLevelUp_whenTheAwardCrossesALevelBoundary() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        // 4th perfect challenge -> 100 XP total, exactly the level-1 threshold
        when(reviewRepository.countPerfectCodeChallengesByUserIdAndCategory(1L, "java", 1.0))
            .thenReturn(4L);

        RewardResponse response =
            rewardService.rewardDefaultUser(1.0, "java", RetryType.CODE_CHALLENGE, false);

        assertThat(response.xp()).isEqualTo(100);
        assertThat(response.level()).isEqualTo(1);
        assertThat(response.leveledUp()).isTrue();
    }

    // "Code coverage" is masteryPercent's sibling: same totalTopics
    // denominator, but the numerator is topics with a perfect, non-retry
    // code challenge instead of a high memory score.
    @Test
    void getCurrentUserReward_computesCodeCoveragePercentFromCoveredTopics() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(topicRepository.countByUserIdAndCategory(1L, "java")).thenReturn(10L);
        when(reviewRepository.countCoveredTopicsByUserIdAndCategory(1L, "java", 1.0)).thenReturn(3L);

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.coveredTopics()).isEqualTo(3L);
        assertThat(response.codeCoveragePercent()).isEqualTo(30);
    }

    @Test
    void getCurrentUserReward_codeCoveragePercentIsZero_whenCategoryHasNoTopics() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.coveredTopics()).isZero();
        assertThat(response.codeCoveragePercent()).isZero();
    }

    @Test
    void getCurrentUserReward_reportsXpLevelAndProgress_withoutAwarding() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        // 10 perfect code challenges in this category -> 250 XP
        when(reviewRepository.countPerfectCodeChallengesByUserIdAndCategory(1L, "java", 1.0))
            .thenReturn(10L);

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.xp()).isEqualTo(250);
        assertThat(response.level()).isEqualTo(1);
        assertThat(response.xpIntoLevel()).isEqualTo(150);
        assertThat(response.xpForNextLevel()).isEqualTo(300);
        assertThat(response.lastXpAwarded()).isZero();
        assertThat(response.leveledUp()).isFalse();
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
            .thenReturn(activityOnOneDay(3, 1.0, RetryType.CODE_CHALLENGE));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.goalDaysThisMonth()).isEqualTo(1);
    }

    @Test
    void getCurrentUserReward_goalDaysIgnoresCodeChallengesScoringLessThanPerfect() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(new UserReward(1L)));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of());
        when(reviewRepository.findGoalActivityByUserIdAndCategorySince(eq(1L), eq("java"), any()))
            .thenReturn(activityOnOneDay(3, 0.5, RetryType.CODE_CHALLENGE));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.goalDaysThisMonth()).isZero();
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
            .thenReturn(activityOnDay(LocalDateTime.now(), 3, 1.0, RetryType.CODE_CHALLENGE));

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

    // XP-per-file/topic is a straight multiplication of the perfect,
    // non-retry code-challenge count by rewardProperties.codeChallengeXp() -
    // these tests just confirm that wiring, the same way
    // rewardDefaultUser_perfectCodeChallenge_awardsCodeChallengeXp confirms
    // it for the category-level total.
    @Test
    void getCodeChallengeXpByMarkdownFile_multipliesPerfectCountsByCodeChallengeXp() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(reviewRepository.countPerfectCodeChallengesByMarkdownFile(1L, "java", 1.0))
            .thenReturn(List.of(
                new TestMarkdownFileCodeChallengeCount(10L, 3L),
                new TestMarkdownFileCodeChallengeCount(20L, 1L)
            ));

        Map<Long, Long> xpByFile = rewardService.getCodeChallengeXpByMarkdownFile("java");

        assertThat(xpByFile)
            .containsEntry(10L, 3L * CODE_CHALLENGE_XP)
            .containsEntry(20L, 1L * CODE_CHALLENGE_XP);
    }

    @Test
    void getCodeChallengeXpByTopicInMarkdownFile_multipliesPerfectCountsByCodeChallengeXp() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(reviewRepository.countPerfectCodeChallengesByTopicInMarkdownFile(1L, 5L, 1.0))
            .thenReturn(List.of(
                new TestTopicCodeChallengeCount(100L, 2L),
                new TestTopicCodeChallengeCount(200L, 0L)
            ));

        Map<Long, Long> xpByTopic = rewardService.getCodeChallengeXpByTopicInMarkdownFile(5L);

        assertThat(xpByTopic)
            .containsEntry(100L, 2L * CODE_CHALLENGE_XP)
            .containsEntry(200L, 0L);
    }

    private record TestMarkdownFileCodeChallengeCount(
        Long markdownFileId,
        Long codeChallengeCount
    ) implements MarkdownFileCodeChallengeCountProjection {

        @Override
        public Long getMarkdownFileId() {
            return markdownFileId;
        }

        @Override
        public Long getCodeChallengeCount() {
            return codeChallengeCount;
        }
    }

    private record TestTopicCodeChallengeCount(
        Long topicId,
        long codeChallengeCount
    ) implements TopicCodeChallengeCountProjection {

        @Override
        public Long getTopicId() {
            return topicId;
        }

        @Override
        public long getCodeChallengeCount() {
            return codeChallengeCount;
        }
    }
}

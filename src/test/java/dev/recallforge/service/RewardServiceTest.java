package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.recallforge.domain.UserReward;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.dto.SubcategoryMasteryResponse;
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
            userRewardRepository, currentUserService, topicRepository, reviewRepository
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

        assertThat(response.gainedXp()).isEqualTo(1);
        assertThat(response.totalXp()).isEqualTo(1);
        assertThat(response.level()).isEqualTo(1);
        assertThat(response.title()).isEqualTo("Learner");
        assertThat(response.streakDays()).isEqualTo(1);
    }

    @Test
    void rewardDefaultUser_perfectScore_addsBonusXpOnTopOfBaseXp() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRewardRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RewardResponse response = rewardService.rewardDefaultUser(0.95, "java");

        assertThat(response.gainedXp()).isEqualTo(15);
        assertThat(response.totalXp()).isEqualTo(15);
    }

    @ParameterizedTest
    @CsvSource({
        "0.39, 1",
        "0.40, 3",
        "0.69, 3",
        "0.70, 6",
        "0.89, 6",
        "0.90, 10",
    })
    void rewardDefaultUser_awardsXpByScoreBand(double score, int expectedXp) {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRewardRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RewardResponse response = rewardService.rewardDefaultUser(score, "java");

        assertThat(response.gainedXp()).isEqualTo(expectedXp);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 1, Learner",
        "150, 2, Student",
        "250, 3, Apprentice",
        "950, 10, Grandmaster",
        "1000, 11, Grandmaster",
    })
    void getCurrentUserReward_mapsTotalXpToLevelAndTitle(int existingXp, int expectedLevel, String expectedTitle) {
        setUp();
        UserReward existingReward = new UserReward(1L);
        existingReward.addReviewReward(existingXp, false, LocalDate.now());

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(existingReward));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.level()).isEqualTo(expectedLevel);
        assertThat(response.title()).isEqualTo(expectedTitle);
        assertThat(response.gainedXp()).isZero();
    }

    @Test
    void getCurrentUserReward_doesNotCreateOrMutateRewardWhenOneAlreadyExists() {
        setUp();
        UserReward existingReward = new UserReward(1L);
        existingReward.addReviewReward(50, false, LocalDate.now());

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(existingReward));

        rewardService.getCurrentUserReward("java");

        verify(userRewardRepository, never()).save(any());
    }

    @Test
    void getCurrentUserReward_streakReadsZero_whenUserMissedMoreThanOneDay() {
        setUp();
        UserReward existingReward = new UserReward(1L);
        existingReward.addReviewReward(50, false, LocalDate.now().minusDays(3));

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
        existingReward.addReviewReward(50, false, LocalDate.now().minusDays(1));

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(userRewardRepository.findByUserId(1L)).thenReturn(Optional.of(existingReward));
        when(reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(1L, "java"))
            .thenReturn(List.of(LocalDateTime.now().minusDays(1)));

        RewardResponse response = rewardService.getCurrentUserReward("java");

        assertThat(response.streakDays()).isEqualTo(1);
    }

    @Test
    void getSubcategoryMastery_computesPercentFromMasteredAndTotalCounts() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicRepository.countByUserIdAndCategoryAndSubcategory(1L, "java", "threads"))
            .thenReturn(8L);
        when(topicRepository.countMasteredByUserIdAndCategoryAndSubcategory(
                eq(1L), eq("java"), eq("threads"), any(Double.class), any()))
            .thenReturn(6L);

        SubcategoryMasteryResponse response = rewardService.getSubcategoryMastery("java", "threads");

        assertThat(response.subcategory()).isEqualTo("threads");
        assertThat(response.masteredTopics()).isEqualTo(6L);
        assertThat(response.totalTopicsInScope()).isEqualTo(8L);
        assertThat(response.masteryPercent()).isEqualTo(75);
    }

    @Test
    void getSubcategoryMastery_returnsZeroPercent_whenSubcategoryHasNoTopics() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicRepository.countByUserIdAndCategoryAndSubcategory(1L, "java", "empty"))
            .thenReturn(0L);

        SubcategoryMasteryResponse response = rewardService.getSubcategoryMastery("java", "empty");

        assertThat(response.masteryPercent()).isZero();
    }
}

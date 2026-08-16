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

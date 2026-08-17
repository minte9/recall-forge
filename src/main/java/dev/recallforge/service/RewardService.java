package dev.recallforge.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.recallforge.domain.UserReward;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.repository.UserRewardRepository;
import dev.recallforge.security.CurrentUserService;

/**
 * Streak/mastery bookkeeping for the current user. Streak and mastery are
 * computed per category from review/topic history; the per-user
 * {@link UserReward} row tracks the streak's day bookkeeping. {@link
 * #rewardDefaultUser} is called by {@link ReviewService} after each graded
 * answer; {@link #getCurrentUserReward} is a read-only variant used by
 * {@code DashboardService} to display current standing without recording a
 * review.
 */
@Service
public class RewardService {

    private static final double MASTERY_MEMORY_SCORE = 0.7;

    private final UserRewardRepository userRewardRepository;
    private final CurrentUserService currentUserService;
    private final TopicRepository topicRepository;
    private final ReviewRepository reviewRepository;

    public RewardService(
        UserRewardRepository userRewardRepository,
        CurrentUserService currentUserService,
        TopicRepository topicRepository,
        ReviewRepository reviewRepository
    ) {
        this.userRewardRepository = userRewardRepository;
        this.currentUserService = currentUserService;
        this.topicRepository = topicRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public RewardResponse rewardDefaultUser(double score, String category) {
        Long userId = currentUserService.currentUserId();

        UserReward reward = userRewardRepository.findByUserId(userId)
            .orElseGet(() -> userRewardRepository.save(new UserReward(userId)));

        boolean perfect = score >= 0.95;

        reward.addReviewReward(perfect, LocalDate.now());
        userRewardRepository.save(reward);

        return toResponse(reward, category);
    }

    @Transactional
    public RewardResponse getCurrentUserReward(String category) {
        Long userId = currentUserService.currentUserId();

        UserReward reward = userRewardRepository.findByUserId(userId)
            .orElseGet(() -> userRewardRepository.save(new UserReward(userId)));

        return toResponse(reward, category);
    }

    private RewardResponse toResponse(
        UserReward reward,
        String category
    ) {
        Long userId = reward.getUserId();

        long totalTopics = topicRepository.countByUserIdAndCategory(userId, category);

        long masteredTopics =
            topicRepository.countMasteredByUserIdAndCategory(
                userId,
                category,
                MASTERY_MEMORY_SCORE,
                LocalDateTime.now()
            );

        int masteryPercent = totalTopics == 0
            ? 0
            : (int) Math.round(masteredTopics * 100.0 / totalTopics);

        return new RewardResponse(
            categoryStreakDays(userId, category, LocalDate.now()),

            category,
            masteredTopics,
            totalTopics,
            masteryPercent
        );
    }

    // Consecutive days (ending today or yesterday) with at least one review
    // in this category. Computed live from review history rather than a
    // stored counter, so it's correct per category with no staleness.
    private int categoryStreakDays(Long userId, String category, LocalDate today) {
        List<LocalDateTime> reviewedAt =
            reviewRepository.findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(userId, category);

        Set<LocalDate> distinctDatesDesc = new LinkedHashSet<>();

        for (LocalDateTime timestamp : reviewedAt) {
            distinctDatesDesc.add(timestamp.toLocalDate());
        }

        if (distinctDatesDesc.isEmpty()) {
            return 0;
        }

        LocalDate mostRecent = distinctDatesDesc.iterator().next();
        boolean streakAlive = mostRecent.equals(today) || mostRecent.equals(today.minusDays(1));

        if (!streakAlive) {
            return 0;
        }

        int streak = 1;
        LocalDate expected = mostRecent.minusDays(1);

        for (LocalDate date : distinctDatesDesc) {
            if (date.equals(mostRecent)) {
                continue;
            }

            if (!date.equals(expected)) {
                break;
            }

            streak++;
            expected = expected.minusDays(1);
        }

        return streak;
    }
}

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
 * XP/level/streak/mastery bookkeeping for the current user. XP/level are
 * global, backed by a single {@link UserReward} row per user; streak and
 * mastery are computed per category from review/topic history. {@link
 * #rewardDefaultUser} is called by {@link ReviewService} after each graded
 * answer; {@link #getCurrentUserReward} is a read-only variant used by
 * {@code DashboardService} to display current standing without granting XP.
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

        int baseXp = xpForScore(score);
        boolean perfect = score >= 0.95;

        int gainedXp = baseXp + (perfect ? 5 : 0);

        reward.addReviewReward(gainedXp, perfect, LocalDate.now());
        userRewardRepository.save(reward);

        return toResponse(reward, gainedXp, category);
    }

    @Transactional
    public RewardResponse getCurrentUserReward(String category) {
        Long userId = currentUserService.currentUserId();

        UserReward reward = userRewardRepository.findByUserId(userId)
            .orElseGet(() -> userRewardRepository.save(new UserReward(userId)));

        return toResponse(reward, 0, category);
    }

    private int xpForScore(double score) {
        if (score < 0.4) return 1;      // Again
        if (score < 0.7) return 3;      // Hard
        if (score < 0.9) return 6;      // Good
        return 10;                      // Easy
    }

    private RewardResponse toResponse(
        UserReward reward,
        int gainedXp,
        String category
    ) {
        int level = reward.getTotalXp() / 100 + 1;
        int xpInLevel = reward.getTotalXp() % 100;

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
            reward.getTotalXp(),
            level,
            titleForLevel(level),
            xpInLevel,
            100,
            categoryStreakDays(userId, category, LocalDate.now()),
            gainedXp,

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

    private String titleForLevel(int level) {
        return switch (Math.min(level, 10)) {
            case 1 -> "Learner";
            case 2 -> "Student";
            case 3 -> "Apprentice";
            case 4 -> "Practitioner";
            case 5 -> "Specialist";
            case 6 -> "Expert";
            case 7 -> "Scholar";
            case 8 -> "Mentor";
            case 9 -> "Master";
            default -> "Grandmaster";
        };
    }
}

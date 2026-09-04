package dev.recallforge.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.recallforge.config.AppGoalProperties;
import dev.recallforge.config.AppRewardProperties;
import dev.recallforge.domain.RetryType;
import dev.recallforge.domain.UserReward;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.repository.ReviewGoalActivityProjection;
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

    // A "goal day" is hit either by grading enough fresh reviews well, or by
    // getting enough code-challenge attempts fully correct (score == 1 -
    // partial credit doesn't count, since the challenge either works or it
    // doesn't). The score bars themselves aren't user-configurable (unlike
    // the review/code-challenge counts, via AppGoalProperties) -
    // GOAL_REVIEW_SCORE_THRESHOLD is kept separate from MASTERY_MEMORY_SCORE
    // even though the value matches today, since the two thresholds
    // represent different concepts and may diverge later.
    private static final double GOAL_REVIEW_SCORE_THRESHOLD = 0.7;
    private static final double GOAL_CODE_CHALLENGE_SCORE_THRESHOLD = 1.0;

    // Different from the one above (used for goal day)
    // This one is used for XP points (levels)
    private static final double CODE_CHALLENGE_XP_SCORE_THRESHOLD = 1.0;

    private final UserRewardRepository userRewardRepository;
    private final CurrentUserService currentUserService;
    private final TopicRepository topicRepository;
    private final ReviewRepository reviewRepository;
    private final AppGoalProperties goalProperties;
    private final AppRewardProperties rewardProperties;

    public RewardService(
        UserRewardRepository userRewardRepository,
        CurrentUserService currentUserService,
        TopicRepository topicRepository,
        ReviewRepository reviewRepository,
        AppGoalProperties goalProperties,
        AppRewardProperties rewardProperties
    ) {
        this.userRewardRepository = userRewardRepository;
        this.currentUserService = currentUserService;
        this.topicRepository = topicRepository;
        this.reviewRepository = reviewRepository;
        this.goalProperties = goalProperties;
        this.rewardProperties = rewardProperties;
    }

    // isRetry is a Try Again / Review Again resubmission of an already-graded
    // question, not a fresh attempt - it's still graded and saved, but never
    // earns XP here, and (via the isRetry = false filters in ReviewRepository)
    // never counts toward goal days or the daily streak either. Otherwise
    // resubmitting an already-perfected code challenge could be farmed for
    // unlimited XP.
    @Transactional
    public RewardResponse rewardDefaultUser(double score, String category, RetryType retryType, boolean isRetry) {
        Long userId = currentUserService.currentUserId();

        UserReward reward = userRewardRepository.findByUserId(userId)
            .orElseGet(() -> userRewardRepository.save(new UserReward(userId)));

        boolean perfect = score >= 0.95;
        reward.addReviewReward(perfect, LocalDate.now());

        long lastXpAwarded = !isRetry
                && retryType == RetryType.CODE_CHALLENGE
                && score >= CODE_CHALLENGE_XP_SCORE_THRESHOLD
            ? rewardProperties.codeChallengeXp()
            : 0;

        userRewardRepository.save(reward);  // -- explicit, but not needed (@Trasactional flushes on commit anyway)

        return toResponse(reward, category, lastXpAwarded);
    }

    @Transactional
    public RewardResponse getCurrentUserReward(String category) {
        Long userId = currentUserService.currentUserId();

        UserReward reward = userRewardRepository.findByUserId(userId)
            .orElseGet(() -> userRewardRepository.save(new UserReward(userId)));

        return toResponse(reward, category, 0);
    }

    private RewardResponse toResponse(
        UserReward reward,
        String category,
        long lastXpAwarded
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

        GoalDaysSummary goalDaysSummary = goalDaysThisMonth(userId, category, LocalDate.now());

        // Code-challenge XP — computed live per category (like streak and goal
        // days above), not stored: it's exactly 25 XP per perfect code challenge
        // in this category, so there's nothing a counter would add but staleness.
        long perfectChallenges = reviewRepository.countPerfectCodeChallengesByUserIdAndCategory(
            userId, category, CODE_CHALLENGE_XP_SCORE_THRESHOLD);
        long xp = perfectChallenges * rewardProperties.codeChallengeXp();

        int level = levelForXp(xp);
        long floorXp = (long) level * level * 100;
        long nextXp = (long) (level + 1) * (level + 1) * 100;

        int previousLevel = levelForXp(xp - lastXpAwarded);
        boolean leveledUp = level > previousLevel;

        return new RewardResponse(
            categoryStreakDays(userId, category, LocalDate.now()),
            goalDaysSummary.count(),
            goalDaysSummary.reachedToday(),
            goalProperties.reviews(),
            goalProperties.codeChallenges(),

            category,
            masteredTopics,
            totalTopics,
            masteryPercent,

            xp,
            level,
            xp - floorXp,
            nextXp - floorXp,
            lastXpAwarded,
            leveledUp
        );
    }

    private static int levelForXp(long xp) {
        return (int) Math.floor(Math.sqrt(Math.max(0, xp) / 100.0));
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

    // Count of days this calendar month (in this category) that hit the
    // daily goal: goalProperties.reviews() fresh reviews scoring at least
    // GOAL_REVIEW_SCORE_THRESHOLD, or goalProperties.codeChallenges()
    // code-challenge attempts scoring at least
    // GOAL_CODE_CHALLENGE_SCORE_THRESHOLD. Resets on the 1st of each month rather than
    // accumulating forever, so the number stays meaningful instead of just
    // climbing - unlike the streak, a missed day doesn't zero it out, it
    // just doesn't add to it. Computed live from review history so it stays
    // correct with no stored counter. Also reports whether today itself is
    // one of those qualifying days - checked here (same qualifying-day set)
    // rather than as a separate query, and read by ReviewService after every
    // answer via rewardDefaultUser to show a "goal reached today" badge.
    private GoalDaysSummary goalDaysThisMonth(Long userId, String category, LocalDate today) {
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();

        List<ReviewGoalActivityProjection> activity =
            reviewRepository.findGoalActivityByUserIdAndCategorySince(userId, category, startOfMonth);

        Map<LocalDate, Integer> highScoreReviewCountByDay = new HashMap<>();
        Map<LocalDate, Integer> codeChallengeCountByDay = new HashMap<>();

        for (ReviewGoalActivityProjection row : activity) {
            LocalDate day = row.getReviewedAt().toLocalDate();

            if (row.getRetryType() == RetryType.CODE_CHALLENGE) {
                if (row.getScore() >= GOAL_CODE_CHALLENGE_SCORE_THRESHOLD) {
                    codeChallengeCountByDay.merge(day, 1, Integer::sum);
                }
            } else if (row.getScore() >= GOAL_REVIEW_SCORE_THRESHOLD) {
                highScoreReviewCountByDay.merge(day, 1, Integer::sum);
            }
        }

        Set<LocalDate> goalDays = new HashSet<>();

        highScoreReviewCountByDay.forEach((day, count) -> {
            if (count >= goalProperties.reviews()) {
                goalDays.add(day);
            }
        });

        codeChallengeCountByDay.forEach((day, count) -> {
            if (count >= goalProperties.codeChallenges()) {
                goalDays.add(day);
            }
        });

        return new GoalDaysSummary(goalDays.size(), goalDays.contains(today));
    }

    private record GoalDaysSummary(int count, boolean reachedToday) {
    }
}

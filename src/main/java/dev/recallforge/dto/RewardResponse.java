package dev.recallforge.dto;

public record RewardResponse(
    int streakDays,
    int goalDaysThisMonth,
    boolean goalReachedToday,
    int goalReviewsPerDay,
    int goalCodeChallengesPerDay,

    String masteryScope,
    long reviewedTopics,
    int reviewedPercent,
    long masteredTopics,
    long totalTopicsInScope,
    int masteryPercent,

    long coveredTopics,
    int codeCoveragePercent,

    long xp,
    int level,
    long xpIntoLevel,
    long xpForNextLevel,
    long lastXpAwarded,  // > 0 only on the call that just awarded it; 0 from the dashboard read 
    boolean leveledUp
) {}

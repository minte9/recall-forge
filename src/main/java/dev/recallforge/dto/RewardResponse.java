package dev.recallforge.dto;

public record RewardResponse(
    int streakDays,
    int goalDaysThisMonth,
    int goalReviewsPerDay,
    int goalCodeChallengesPerDay,

    String masteryScope,
    long masteredTopics,
    long totalTopicsInScope,
    int masteryPercent
) {}

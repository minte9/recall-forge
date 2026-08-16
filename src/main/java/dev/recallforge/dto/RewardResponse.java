package dev.recallforge.dto;

public record RewardResponse(
    int streakDays,

    String masteryScope,
    long masteredTopics,
    long totalTopicsInScope,
    int masteryPercent
) {}

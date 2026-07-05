package dev.recallforge.dto;

public record RewardResponse(
    int totalXp,
    int level,
    String title,
    int xpInLevel, 
    int xpForNextLevel,
    int streakDays,
    int gainedXp,

    String masteryScope,
    long masteredTopics,
    long totalTopicsInScope,
    int masteryPercent
) {}

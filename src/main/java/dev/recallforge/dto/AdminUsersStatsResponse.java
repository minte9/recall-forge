package dev.recallforge.dto;

public record AdminUsersStatsResponse(
    Long userId,
    String email,
    String displayName, 
    boolean admin, 
    Long totalTopics,
    Long totalReviews,
    Long totalTokenSpent
) {}

package dev.recallforge.dto;

import dev.recallforge.domain.AiModel;

public record CurrentUserResponse(
    Long id,
    String email,
    boolean admin,
    String displayName,
    String role,
    String provider,
    String avatarUrl,
    int dailyReviewLimit,
    int dailyQuestionLimit,
    int dailyUserReviewLimit,
    int dailyUserQuestionLimit,
    int dailyOptimizeLimit,
    int maxTopicsPerUser,
    boolean aiEnabled,
    AiModel aiModel
) {}

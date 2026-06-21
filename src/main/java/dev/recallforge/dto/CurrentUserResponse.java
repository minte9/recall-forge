package dev.recallforge.dto;

public record CurrentUserResponse(
    Long id,
    String email,
    String displayName,
    String role,
    String avatarUrl
) {}

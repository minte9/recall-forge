package dev.recallforge.dto;

public record CurrentUserResponse(
    Long id,
    String email,
    boolean admin, 
    String displayName,
    String role,
    String avatarUrl
) {}

package dev.recallforge.dto;

public record CategoryOptionResponse(
    String environment,
    String category,
    boolean isPrivate
) {}

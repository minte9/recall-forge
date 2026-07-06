package dev.recallforge.dto;

public record AiResponse(
    String content,
    int promptTokens,
    int completionTokens,
    int totalTokens
) {}
package dev.recallforge.dto;

public record WeakAreaResponse (
    String environment,
    String category,
    String subcategory,
    long dueCount,
    double averageMemoryScore
) {}
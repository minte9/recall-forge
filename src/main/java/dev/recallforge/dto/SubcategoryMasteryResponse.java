package dev.recallforge.dto;

public record SubcategoryMasteryResponse(
    String subcategory,
    long masteredTopics,
    long totalTopicsInScope,
    int masteryPercent
) {}

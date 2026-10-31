package dev.recallforge.dto;

public record RecallInsightResponse(
    String category,
    String message,
    long topicCount,
    long reviewedTopicCount,
    double averageMemoryScore,
    String weakestTopic,
    String weakestSubcategory
) {}
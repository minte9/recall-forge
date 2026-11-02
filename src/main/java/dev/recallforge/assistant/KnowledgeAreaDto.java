package dev.recallforge.assistant;

public record KnowledgeAreaDto(
    String category,
    String subcategory,
    long topicCount,
    Double averageMemoryScore,
    long masteredTopics
) {}
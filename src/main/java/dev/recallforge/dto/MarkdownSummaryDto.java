package dev.recallforge.dto;

public record MarkdownSummaryDto(
    Long markdownFileId,
    String category,
    String subcategory,
    String fileTitle,
    Long topicCont,
    Long dueCount,
    Double averageMemoryScore
) {}

package dev.recallforge.dto;

public record MarkdownSummaryDto(
    Long markdownFileId,
    String environment,
    String category,
    String subcategory,
    String fileTitle,
    Long topicCont,
    Long dueCount,
    Double averageMemoryScore,
    Long codeChallengeCount
) {
    // Matches TopicRepository.findMarkdownSummaries' JPQL constructor
    // expression, which doesn't join into reviews - codeChallengeCount is
    // filled in afterwards from a separate query in DashboardService.
    public MarkdownSummaryDto(
        Long markdownFileId,
        String environment,
        String category,
        String subcategory,
        String fileTitle,
        Long topicCont,
        Long dueCount,
        Double averageMemoryScore
    ) {
        this(markdownFileId, environment, category, subcategory, fileTitle, topicCont, dueCount,
            averageMemoryScore, 0L);
    }
}

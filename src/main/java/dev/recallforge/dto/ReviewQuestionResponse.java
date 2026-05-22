package dev.recallforge.dto;

public record ReviewQuestionResponse(
    Long topicId,
    String topicTitle,
    String question
) {
}

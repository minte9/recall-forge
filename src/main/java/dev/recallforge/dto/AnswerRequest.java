package dev.recallforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
    @NotNull
    Long topicId,

    @NotBlank
    String question,

    @NotBlank
    String userAnswer
) {
}

package dev.recallforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
    @NotNull
    Long topicId,

    @NotBlank
    String question,

    @NotBlank
    String userAnswer,

    // True when this is a same-sitting resubmission of the currently
    // displayed question (the "Try Again" flow), not a fresh review -
    // it's graded and saved, but must not move the spaced-repetition state.
    boolean isRetry
) {
}

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
    // displayed question (the "Try Again" flow) or a "Code Challenge"
    // answer - either way it's practice, not a fresh review: graded and
    // saved, but must not move the spaced-repetition state.
    boolean isRetry,

    // Set only for a "Code Challenge" answer - the original code shown to
    // the user, round-tripped so it can be re-sent for strict grading.
    // Null for a normal review answer.
    String codeSnippet
) {
}

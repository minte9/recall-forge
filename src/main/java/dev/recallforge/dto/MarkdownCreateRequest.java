package dev.recallforge.dto;

import jakarta.validation.constraints.NotBlank;

public record MarkdownCreateRequest(
    @NotBlank
    String content,

    // Set only when this submit follows an accepted Optimize preview,
    // unchanged since - null for a plain manual entry.
    AppliedOptimization appliedOptimization,

    // The pre-optimize content, required only when appliedOptimization is
    // STRUCTURED (ignored otherwise) - stored as the original snapshot the
    // first time this file is optimized.
    String originalContent
) {
}

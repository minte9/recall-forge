package dev.recallforge.dto;

import jakarta.validation.constraints.NotBlank;

public record MarkdownOriginalUpdateRequest(
    @NotBlank
    String content
) {
}

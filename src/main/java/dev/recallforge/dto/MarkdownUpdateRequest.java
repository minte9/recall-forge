package dev.recallforge.dto;

import jakarta.validation.constraints.NotBlank;

public record MarkdownUpdateRequest(
    @NotBlank
    String content
) {
}

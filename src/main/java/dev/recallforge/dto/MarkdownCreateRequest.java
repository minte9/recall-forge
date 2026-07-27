package dev.recallforge.dto;

import jakarta.validation.constraints.NotBlank;

public record MarkdownCreateRequest(
    @NotBlank
    String content
) {
}

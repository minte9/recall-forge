package dev.recallforge.dto;

import jakarta.validation.constraints.NotBlank;

public record MarkdownOptimizeRequest(
    @NotBlank
    String content
) {
}

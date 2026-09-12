package dev.recallforge.service;

/**
 * Result of {@link MarkdownFileService#optimize}. {@code structured}
 * distinguishes which branch produced {@code content} - the caller (the
 * controller, then the frontend) carries it through so a later create/update
 * call knows whether to record {@code STRUCTURED} (capture an original
 * snapshot) or {@code GENERATED} (no snapshot) via {@link
 * dev.recallforge.dto.AppliedOptimization}.
 */
public record OptimizeResult(String content, boolean structured) {
}

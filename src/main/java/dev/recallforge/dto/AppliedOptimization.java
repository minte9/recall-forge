package dev.recallforge.dto;

/**
 * Which branch of {@link dev.recallforge.service.MarkdownFileService#optimize}
 * produced the content being submitted, sent back by the client alongside a
 * create/update call that follows an accepted Optimize preview (unchanged
 * since). Null/absent means the save is a plain manual edit.
 */
public enum AppliedOptimization {
    STRUCTURED,
    GENERATED
}

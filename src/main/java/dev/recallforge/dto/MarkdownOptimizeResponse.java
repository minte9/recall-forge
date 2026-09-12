package dev.recallforge.dto;

public record MarkdownOptimizeResponse(
    String content,

    // True when the structured (topic-body-rewrite) branch was used, false
    // when the whole file was AI-generated from unstructured raw notes -
    // tells the client which flag/snapshot to send back on Submit.
    boolean structured
) {
}

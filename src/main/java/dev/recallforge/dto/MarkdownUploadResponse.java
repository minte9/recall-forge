package dev.recallforge.dto;

public record MarkdownUploadResponse(
        Long markdownFileId,
        String filename,
        String content,
        String originalContent,
        boolean optimized,
        boolean aiGenerated
) {
}
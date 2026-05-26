package dev.recallforge.dto;

public record MarkdownUploadResponse(
        Long markdownFileId,
        String filename,
        String content
) {
}
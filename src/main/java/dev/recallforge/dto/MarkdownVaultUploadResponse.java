package dev.recallforge.dto;

import java.util.List;

/**
 * Aggregate result of a vault (multi-file) markdown import. One bad file
 * (too large, empty, malformed, invalid image) is recorded in {@code
 * errors} and skipped rather than aborting the rest of the vault.
 * {@code importedImages} lists the filenames of images stored.
 */
public record MarkdownVaultUploadResponse(
        List<MarkdownUploadResponse> imported,
        List<String> importedImages,
        List<VaultFileError> errors
) {
    public record VaultFileError(String path, String message) {
    }
}

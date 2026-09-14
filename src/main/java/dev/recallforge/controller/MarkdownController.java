package dev.recallforge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.recallforge.dto.CodeChallengeResponse;
import dev.recallforge.dto.MarkdownCreateRequest;
import dev.recallforge.dto.MarkdownOptimizeRequest;
import dev.recallforge.dto.MarkdownOptimizeResponse;
import dev.recallforge.dto.MarkdownOriginalUpdateRequest;
import dev.recallforge.dto.MarkdownReviewCountResponse;
import dev.recallforge.dto.MarkdownUpdateRequest;
import dev.recallforge.dto.MarkdownUploadResponse;
import dev.recallforge.dto.MarkdownVaultUploadResponse;
import dev.recallforge.security.CurrentUserService;
import dev.recallforge.service.MarkdownFileService;
import dev.recallforge.service.OptimizeResult;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/markdown")
public class MarkdownController {

    private final MarkdownFileService markdownFileService;
    private final CurrentUserService currentUserService;

    public MarkdownController(MarkdownFileService markdownFileService, CurrentUserService currentUserService) {
        this.markdownFileService = markdownFileService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/upload")
    public MarkdownUploadResponse uploadMarkdown(@RequestParam("file") MultipartFile file) throws Exception {
        return markdownFileService.upload(file);
    }

    @PostMapping("/upload-vault")
    public MarkdownVaultUploadResponse uploadVault(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("paths") List<String> paths
    ) throws Exception {
        return markdownFileService.uploadVault(files, paths);
    }

    @PostMapping
    public MarkdownUploadResponse createMarkdown(@Valid @RequestBody MarkdownCreateRequest request) throws Exception {
        return markdownFileService.createFromContent(
                request.content(), request.appliedOptimization(), request.originalContent());
    }

    @GetMapping("/{id}")
    public MarkdownUploadResponse getMarkdown(@PathVariable Long id) {
        return markdownFileService.getById(id);
    }

    @PutMapping("/{id}")
    public MarkdownUploadResponse updateMarkdown(
            @PathVariable Long id,
            @Valid @RequestBody MarkdownUpdateRequest request
    ) throws Exception {
        return markdownFileService.update(
                id, request.content(), request.appliedOptimization(), request.originalContent());
    }

    @PutMapping("/{id}/original")
    public MarkdownUploadResponse updateMarkdownOriginal(
            @PathVariable Long id,
            @Valid @RequestBody MarkdownOriginalUpdateRequest request
    ) {
        return markdownFileService.updateOriginal(id, request.content());
    }

    @DeleteMapping("/{id}/original")
    public MarkdownUploadResponse deleteMarkdownOriginal(@PathVariable Long id) {
        return markdownFileService.deleteOriginal(id);
    }

    @PostMapping("/optimize")
    public MarkdownOptimizeResponse optimizeMarkdown(@Valid @RequestBody MarkdownOptimizeRequest request) {
        OptimizeResult result = markdownFileService.optimize(request.content());
        return new MarkdownOptimizeResponse(result.content(), result.structured());
    }

    @DeleteMapping("/{id}/reviews")
    public ResponseEntity<Void> resetMarkdownReviews(@PathVariable Long id) {
        currentUserService.resetCurrentUserReviewsForMarkdownFile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/reviews/count")
    public MarkdownReviewCountResponse getMarkdownReviewCount(@PathVariable Long id) {
        return new MarkdownReviewCountResponse(markdownFileService.countReviews(id));
    }

    @GetMapping("/{id}/reviews/code-challenges")
    public List<CodeChallengeResponse> getMarkdownCodeChallenges(@PathVariable Long id) {
        return markdownFileService.getCodeChallenges(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMarkdown(@PathVariable Long id) {
        markdownFileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

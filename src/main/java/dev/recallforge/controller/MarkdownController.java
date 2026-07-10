package dev.recallforge.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.recallforge.dto.MarkdownUpdateRequest;
import dev.recallforge.dto.MarkdownUploadResponse;
import dev.recallforge.service.MarkdownFileService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/markdown")
public class MarkdownController {

    private final MarkdownFileService markdownFileService;

    public MarkdownController(MarkdownFileService markdownFileService) {
        this.markdownFileService = markdownFileService;
    }

    @PostMapping("/upload")
    public MarkdownUploadResponse uploadMarkdown(@RequestParam("file") MultipartFile file) throws Exception {
        return markdownFileService.upload(file);
    }

    @PutMapping("/{id}")
    public MarkdownUploadResponse updateMarkdown(
            @PathVariable Long id,
            @Valid @RequestBody MarkdownUpdateRequest request
    ) throws Exception {
        return markdownFileService.update(id, request.content());
    }
}

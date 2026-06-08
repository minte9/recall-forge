package dev.recallforge.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.recallforge.dto.MarkdownUploadResponse;
import dev.recallforge.service.MarkdownFileService;

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
}

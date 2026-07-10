package dev.recallforge.service;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.repository.MarkdownFileRepository;
import dev.recallforge.security.CurrentUserService;
import dev.recallforge.service.MarkdownFileService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StarterPackService {

    private static final Long TEMPLATE_USER_ID = 1L;

    private final CurrentUserService currentUserService;
    private final MarkdownFileRepository markdownFileRepository;
    private final MarkdownFileService markdownFileService;

    public StarterPackService(
        CurrentUserService currentUserService,
        MarkdownFileRepository markdownFileRepository,
        MarkdownFileService markdownFileService
    ) {
        this.currentUserService = currentUserService;
        this.markdownFileRepository = markdownFileRepository;
        this.markdownFileService = markdownFileService;
    }

    @Transactional
    public void importStarterPack(String category) throws Exception {
        Long currentUserId = currentUserService.currentUserId();

        // Guard rule (template user cannot reset and reimport template markdowns)
        if (TEMPLATE_USER_ID.equals(currentUserId)) {
            throw new IllegalStateException("Template user cannot import starter packs.");
        }

        List<MarkdownFile> templateFiles =
            markdownFileRepository.findByUserIdAndCategory(TEMPLATE_USER_ID, category);

        if (templateFiles.isEmpty()) {
            throw new IllegalArgumentException("Starter pack not found: " + category);
        }

        for (MarkdownFile templateFile : templateFiles) {
            markdownFileService.importMarkdown(templateFile.getContent());
        }
    }
}
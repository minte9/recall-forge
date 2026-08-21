package dev.recallforge.service;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.repository.MarkdownFileRepository;
import dev.recallforge.security.CurrentUserService;
import dev.recallforge.service.MarkdownFileService;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Imports predefined "starter pack" markdown files into a user's account.
 *
 * Starter packs are stored as {@link MarkdownFile} records owned by a fixed
 * template user (id {@value #TEMPLATE_USER_ID}), grouped by category. Only
 * categories in {@link #PUBLIC_CATEGORIES} are offered as starter packs -
 * every other category, and every other user's content, is private by
 * default. When a regular user imports a pack, each template file's content
 * is cloned into the current user's own markdown files via
 * {@link MarkdownFileService}. The template user itself is not allowed to
 * import starter packs.
 */
@Service
public class StarterPackService {

    private static final Long TEMPLATE_USER_ID = 1L;

    // Hardcoded for now - mirrors the buttons in the Starter Pack panel
    // (static/index.html). The only categories a non-template user can ever
    // see as "public"; everything else defaults to private.
    private static final Set<String> PUBLIC_CATEGORIES =
            Set.of("Java", "Python", "Git", "SQL", "Javascript");

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

        if (!isPublicCategory(category)) {
            throw new IllegalArgumentException("Starter pack not found: " + category);
        }

        List<MarkdownFile> templateFiles =
            markdownFileRepository.findByUserIdAndCategory(TEMPLATE_USER_ID, category);

        if (templateFiles.isEmpty()) {
            throw new IllegalArgumentException("Starter pack not found: " + category);
        }

        for (MarkdownFile templateFile : templateFiles) {
            markdownFileService.importMarkdown(templateFile.getContent(), templateFile.getDisplayOrder());
        }
    }

    /**
     * Whether {@code category} is publicly offered as a starter pack for the
     * given owner. Only the template user's own {@link #PUBLIC_CATEGORIES}
     * qualify - every other (owner, category) pair, including all of a
     * regular user's own categories, is private by definition.
     */
    public boolean isPublicCategory(Long ownerId, String category) {
        return TEMPLATE_USER_ID.equals(ownerId)
                && PUBLIC_CATEGORIES.stream().anyMatch(category::equalsIgnoreCase);
    }

    private boolean isPublicCategory(String category) {
        return isPublicCategory(TEMPLATE_USER_ID, category);
    }
}
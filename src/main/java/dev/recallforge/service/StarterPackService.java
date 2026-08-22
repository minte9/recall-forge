package dev.recallforge.service;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.repository.MarkdownFileRepository;
import dev.recallforge.security.CurrentUserService;
import dev.recallforge.service.MarkdownFileService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

    // Hardcoded for now - mirrors the two button rows in the Starter Pack
    // panel (static/index.html): row 1 is the language/tooling fundamentals,
    // row 2 is the matching framework/tool per environment (Spring for JVM,
    // FastAPI for Python, etc). The only categories a non-template user can
    // ever see as "public"; everything else defaults to private.
    private static final Set<String> PUBLIC_CATEGORIES =
            Set.of(
                "Java", "Python", "Git", "SQL", "Javascript",
                "Spring", "FastAPI", "Docker", "MySQL", "Node.js"
            );

    // Row 2 unlocks once the current user's mastery in its row 1 pair (same
    // environment - see PUBLIC_CATEGORIES above) crosses this threshold.
    private static final int UNLOCK_MASTERY_THRESHOLD_PERCENT = 50;

    private static final Map<String, String> ROW_TWO_PREREQUISITE_CATEGORY =
            Map.of(
                "Spring", "Java",
                "FastAPI", "Python",
                "Docker", "Git",
                "MySQL", "SQL",
                "Node.js", "Javascript"
            );

    private final CurrentUserService currentUserService;
    private final MarkdownFileRepository markdownFileRepository;
    private final MarkdownFileService markdownFileService;
    private final RewardService rewardService;

    public StarterPackService(
        CurrentUserService currentUserService,
        MarkdownFileRepository markdownFileRepository,
        MarkdownFileService markdownFileService,
        RewardService rewardService
    ) {
        this.currentUserService = currentUserService;
        this.markdownFileRepository = markdownFileRepository;
        this.markdownFileService = markdownFileService;
        this.rewardService = rewardService;
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

    /**
     * The current user's mastery percent in each starter-pack category (both
     * rows), for display as a badge on the Starter Pack buttons. A category
     * the user hasn't imported yet has 0% mastery.
     */
    public Map<String, Integer> getMasteryPercentages() {
        return PUBLIC_CATEGORIES.stream()
            .collect(Collectors.toMap(
                category -> category,
                category -> rewardService.getCurrentUserReward(category).masteryPercent()
            ));
    }

    /**
     * Whether each row 2 category is unlocked for the current user: true once
     * mastery in its row 1 prerequisite (see {@link
     * #ROW_TWO_PREREQUISITE_CATEGORY}) reaches {@link
     * #UNLOCK_MASTERY_THRESHOLD_PERCENT}. A prerequisite the user hasn't
     * imported yet has 0% mastery, so it starts locked.
     */
    public Map<String, Boolean> getUnlockStatus() {
        Map<String, Integer> masteryPercentages = getMasteryPercentages();

        return ROW_TWO_PREREQUISITE_CATEGORY.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> masteryPercentages.getOrDefault(entry.getValue(), 0)
                    >= UNLOCK_MASTERY_THRESHOLD_PERCENT
            ));
    }
}
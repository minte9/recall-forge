package dev.recallforge.service;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.repository.MarkdownFileRepository;
import dev.recallforge.security.CurrentUserService;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    // Hardcoded for now - mirrors the Starter Pack panel's button rows
    // (static/index.html): a level 1 category (Java, Python, Git, SQL,
    // Javascript) is always importable; every category listed under it here
    // unlocks together, flatly, once level 1's reviewed percent crosses
    // UNLOCK_REVIEWED_THRESHOLD_PERCENT - there's no further chaining between
    // them (each dependent gates directly on its level 1 category). Every
    // category across every entry is the full set a non-template user can
    // ever see as "public"; everything else defaults to private.
    private static final Map<String, List<String>> DEPENDENTS_BY_LEVEL_ONE_CATEGORY =
            Map.of(
                "Java", List.of("Spring"),
                "Python", List.of("Algorithms"),
                "Git", List.of("Docker"),
                "SQL", List.of("PostgreSQL"),
                "Javascript", List.of("TypeScript", "Angular"),
                "Security", List.of("HTTPS"),
                "REST", List.of("GraphQL", "MCP")
            );
    
    @SuppressWarnings("null")
    private static final Set<String> PUBLIC_CATEGORIES =
            Stream.concat(
                DEPENDENTS_BY_LEVEL_ONE_CATEGORY.keySet().stream(),
                DEPENDENTS_BY_LEVEL_ONE_CATEGORY.values().stream().flatMap(List::stream)
            ).collect(Collectors.toUnmodifiableSet());

    // A level 1 category is always importable; everything under it unlocks
    // once the current user's reviewed percent in it crosses this threshold.
    private static final int UNLOCK_REVIEWED_THRESHOLD_PERCENT = 30;

    // dependent category -> its level 1 category (see
    // DEPENDENTS_BY_LEVEL_ONE_CATEGORY above). Level 1 categories themselves
    // have no entry here and so aren't gated - they're always importable.
    @SuppressWarnings("null")
    private static final Map<String, String> LEVEL_ONE_PREREQUISITE_CATEGORY =
            DEPENDENTS_BY_LEVEL_ONE_CATEGORY.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                    .map(dependent -> new SimpleEntry<>(dependent, entry.getKey())))
                .collect(Collectors.toUnmodifiableMap(SimpleEntry::getKey, SimpleEntry::getValue));

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
            markdownFileService.importMarkdown(
                    templateFile.getContent(), templateFile.getDisplayOrder(), templateFile);
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
     * The current user's reviewed percent in each starter-pack category
     * (every row), for display as a badge on the Starter Pack buttons. A
     * category the user hasn't imported yet is 0% reviewed.
     */
    public Map<String, Integer> getReviewedPercentages() {
        return PUBLIC_CATEGORIES.stream()
            .collect(Collectors.toMap(
                category -> category,
                category -> rewardService.getCurrentUserReward(category).reviewedPercent()
            ));
    }

    /**
     * Whether each non-level-1 category is unlocked for the current user:
     * true once the reviewed percent in its level 1 category (see {@link
     * #LEVEL_ONE_PREREQUISITE_CATEGORY}) reaches {@link
     * #UNLOCK_REVIEWED_THRESHOLD_PERCENT}. A level 1 category the user hasn't
     * imported yet is 0% reviewed, so everything under it starts locked.
     */
    @SuppressWarnings("null")
    public Map<String, Boolean> getUnlockStatus() {
        Map<String, Integer> reviewedPercentages = getReviewedPercentages();

        return LEVEL_ONE_PREREQUISITE_CATEGORY.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> reviewedPercentages.getOrDefault(entry.getValue(), 0)
                    >= UNLOCK_REVIEWED_THRESHOLD_PERCENT
            ));
    }
}
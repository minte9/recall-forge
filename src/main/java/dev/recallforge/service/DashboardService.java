package dev.recallforge.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dev.recallforge.config.AppReviewLimitProperties;
import dev.recallforge.domain.CategoryOrder;
import dev.recallforge.domain.SubcategoryOrder;
import dev.recallforge.dto.CategoryOptionResponse;
import dev.recallforge.dto.DashboardResponse;
import dev.recallforge.dto.KnowledgeAreaResponse;
import dev.recallforge.dto.MarkdownSummaryDto;
import dev.recallforge.dto.WeakAreaResponse;
import dev.recallforge.repository.CategoryOrderRepository;
import dev.recallforge.repository.EnvironmentCategoryProjection;
import dev.recallforge.repository.KnowledgeAreaProjection;
import dev.recallforge.repository.MarkdownFileCodeChallengeCountProjection;
import dev.recallforge.repository.QuestionGenerationRepository;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.SubcategoryOrderRepository;
import dev.recallforge.repository.SubcategoryReviewCountProjection;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.repository.WeakAreaProjection;
import dev.recallforge.security.CurrentUserService;

/**
 * Read-only aggregation for the dashboard view: due-topic counts, weak/strong
 * knowledge areas, per-markdown-file review summaries, category options, and
 * daily-review-limit projections. Delegates XP/streak data to
 * {@link RewardService} rather than duplicating it.
 */
@Service
public class DashboardService {

    // A subcategory needs at least this many reviews before its average
    // memory score is trusted enough to call it "strong" or "weak" —
    // otherwise a single review is enough to move it off the shared
    // default score and make it look like a meaningful outlier.
    private static final long MIN_REVIEWS_FOR_KNOWLEDGE_INSIGHTS = 3;

    private final TopicRepository topicRepository;
    private final ReviewRepository reviewRepository;
    private final QuestionGenerationRepository questionGenerationRepository;
    private final RewardService rewardService;
    private final CurrentUserService currentUserService;
    private final SubcategoryOrderRepository subcategoryOrderRepository;
    private final CategoryOrderRepository categoryOrderRepository;
    private final AppReviewLimitProperties reviewLimitProperties;
    private final StarterPackService starterPackService;

    public DashboardService(
        TopicRepository topicRepository,
        ReviewRepository reviewRepository,
        QuestionGenerationRepository questionGenerationRepository,
        RewardService rewardService,
        CurrentUserService currentUserService,
        SubcategoryOrderRepository subcategoryOrderRepository,
        CategoryOrderRepository categoryOrderRepository,
        AppReviewLimitProperties reviewLimitProperties,
        StarterPackService starterPackService
    ) {
        this.topicRepository = topicRepository;
        this.reviewRepository = reviewRepository;
        this.questionGenerationRepository = questionGenerationRepository;
        this.rewardService = rewardService;
        this.currentUserService = currentUserService;
        this.subcategoryOrderRepository = subcategoryOrderRepository;
        this.categoryOrderRepository = categoryOrderRepository;
        this.reviewLimitProperties = reviewLimitProperties;
        this.starterPackService = starterPackService;
    }
    
    public DashboardResponse getDashboard(String category) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1);
        LocalDateTime weekEnd = now.plusDays(7);

        Long userId = currentUserService.currentUserId();

        long dueNow = topicRepository.countDueNowByUser(userId, category, now);
        long dueTomorrow = topicRepository.countDueBetweenByUser(userId, category, tomorrowStart, tomorrowEnd);
        long dueThisWeek = topicRepository.countDueBetweenByUser(userId, category, tomorrowStart, weekEnd);
        long totalTopics = topicRepository.countByUser(userId, category);


        Map<String, Long> reviewCountBySubcategory = reviewRepository.countReviewsBySubcategory(userId, category)
            .stream()
            .collect(Collectors.toMap(
                SubcategoryReviewCountProjection::getSubcategory,
                SubcategoryReviewCountProjection::getReviewCount
            ));

        Map<String, Integer> subcategoryOrder = subcategoryOrderRepository.findByCategoryIgnoreCase(category)
            .stream()
            .collect(Collectors.toMap(SubcategoryOrder::getSubcategory, SubcategoryOrder::getDisplayOrder));

        // findWeakAreas only considers subcategories that have a topic which
        // is both due and reviewed before (see the query) — a topic due for
        // the very first time isn't a "weak area," it's just unstarted. That
        // is the same "reviewed-and-due" pool Start Review works through
        // first, so order these the way Start Review will actually reach
        // them: by subcategory display order (same SubcategoryOrder
        // table/alphabetical fallback as getMarkdownSummaries and
        // TopicService.selectNextTopic), then take the first few. The first
        // row is the subcategory Start Review picks next — not whichever
        // subcategory happens to have the most due topics (the query's
        // busiest-first ordering now only acts as a stable tiebreaker for
        // subcategories with no configured display order).
        List<WeakAreaResponse> weakAreas = topicRepository.findWeakAreas(userId, category, now)
            .stream()
            .sorted(Comparator.<WeakAreaProjection, Integer>comparing(
                    area -> subcategoryOrder.getOrDefault(area.getSubcategory(), Integer.MAX_VALUE))
                .thenComparing(WeakAreaProjection::getSubcategory, String.CASE_INSENSITIVE_ORDER))
            .limit(3)
            .map(area -> new WeakAreaResponse(
                area.getEnvironment(),
                area.getCategory(),
                area.getSubcategory(),
                area.getDueCount(),
                area.getAverageMemoryScore()
            ))
            .toList();

        List<KnowledgeAreaResponse> strongCandidates = topicRepository.findStrongKnowledgeAreas(userId, category)
            .stream()
            .filter(area -> hasEnoughReviewsForInsights(area, reviewCountBySubcategory))
            .limit(2)
            .map(area -> new KnowledgeAreaResponse(
                area.getTitle(),
                area.getAverageMemoryScore()
            ))
            .toList();

        List<KnowledgeAreaResponse> weakCandidates = topicRepository.findWeakKnowledgeAreas(userId, category)
            .stream()
            .filter(area -> hasEnoughReviewsForInsights(area, reviewCountBySubcategory))
            .limit(2)
            .map(area -> new KnowledgeAreaResponse(
                area.getTitle(),
                area.getAverageMemoryScore()
            ))
            .toList();

        // A subcategory that lands in both top-2 lists (common when only a
        // few subcategories qualify at all) isn't distinctly strong or weak
        // — it's just the middle of a small pack. Drop it from both rather
        // than showing it as simultaneously "Strong" and "Weak".
        Set<String> weakTitles = weakCandidates.stream()
            .map(area -> area.title())
            .collect(Collectors.toSet());

        List<KnowledgeAreaResponse> strongOnly = strongCandidates.stream()
            .filter(area -> !weakTitles.contains(area.title()))
            .toList();

        Set<String> strongTitles = strongCandidates.stream()
            .map(area -> area.title())
            .collect(Collectors.toSet());

        List<KnowledgeAreaResponse> weakOnly = weakCandidates.stream()
            .filter(area -> !strongTitles.contains(area.title()))
            .toList();

        // True only when there's a genuine strong/weak contrast between subcategories
        // that have each individually cleared MIN_REVIEWS_FOR_KNOWLEDGE_INSIGHTS —
        // strongCandidates/weakCandidates above are already filtered down to those,
        // so a single review to one otherwise-untouched subcategory can no longer
        // masquerade as a meaningful outlier.
        boolean showKnowledgeInsights =
            !strongOnly.isEmpty() && !weakOnly.isEmpty();

        int dailyReviewLimit = reviewLimitProperties.dailyLimit();
        long reviewsToday = reviewRepository.countByUserIdAndCategoryAndReviewedAtAfter(
            userId, category, LocalDate.now().atStartOfDay());
        long questionsToday = questionGenerationRepository.countByUserIdAndCategoryAndGeneratedAtAfter(
            userId, category, LocalDate.now().atStartOfDay());

        // Tomorrow/this week aren't separately enforced — they show what's realistically
        // reachable given the daily cap. Today's overdue backlog counts too: an overdue
        // topic stays reviewable indefinitely, so it can still fill tomorrow's (or next
        // week's) quota even though its nextReviewAt isn't actually inside that window.
        // Today's own limit is capped the same way, using what's already been reviewed
        // today plus what's still due — otherwise a category with fewer topics than the
        // daily cap would show a denominator it could never actually reach.
        long todayReviewLimit = Math.min(dailyReviewLimit, dueNow + reviewsToday);
        long tomorrowReviewLimit = Math.min(dailyReviewLimit, dueNow + dueTomorrow);
        long thisWeekReviewLimit = Math.min((long) dailyReviewLimit * 7, dueNow + dueThisWeek);

        return new DashboardResponse(
            dueNow,
            dueTomorrow,
            dueThisWeek,
            totalTopics,
            weakAreas,
            strongOnly,
            weakOnly,
            rewardService.getCurrentUserReward(category),
            showKnowledgeInsights,
            dailyReviewLimit,
            reviewsToday,
            todayReviewLimit,
            questionsToday,
            tomorrowReviewLimit,
            thisWeekReviewLimit
        );
    }

    private static boolean hasEnoughReviewsForInsights(
        KnowledgeAreaProjection area,
        Map<String, Long> reviewCountBySubcategory
    ) {
        return reviewCountBySubcategory.getOrDefault(area.getTitle(), 0L) >= MIN_REVIEWS_FOR_KNOWLEDGE_INSIGHTS;
    }

    public List<MarkdownSummaryDto> getMarkdownSummaries(String category) {
        Long userId = currentUserService.currentUserId();

        List<MarkdownSummaryDto> summaries =
            topicRepository.findMarkdownSummaries(userId, category, LocalDateTime.now());

        // Separate query rather than joining reviews into findMarkdownSummaries -
        // a join there would multiply topic rows for topics with more than one
        // code-challenge retry, inflating topicCont/dueCount/averageMemoryScore.
        Map<Long, Long> codeChallengeCountsByFile = reviewRepository
            .countCodeChallengesByMarkdownFile(userId, category)
            .stream()
            .collect(Collectors.toMap(
                MarkdownFileCodeChallengeCountProjection::getMarkdownFileId,
                MarkdownFileCodeChallengeCountProjection::getCodeChallengeCount));

        Map<String, Integer> subcategoryOrder = subcategoryOrderRepository.findByCategoryIgnoreCase(category)
            .stream()
            .collect(Collectors.toMap(SubcategoryOrder::getSubcategory, SubcategoryOrder::getDisplayOrder));

        Map<Long, Long> codeChallengeXpByFile = rewardService.getCodeChallengeXpByMarkdownFile(category);

        // Stable sort: subcategories with no explicit order fall back to
        // the alphabetical order the query already returned them in.
        return summaries.stream()
            .map(s -> new MarkdownSummaryDto(
                s.markdownFileId(), s.environment(), s.category(), s.subcategory(), s.fileTitle(),
                s.topicCont(), s.dueCount(), s.averageMemoryScore(),
                codeChallengeCountsByFile.getOrDefault(s.markdownFileId(), 0L),
                codeChallengeXpByFile.getOrDefault(s.markdownFileId(), 0L)))
            .sorted(Comparator.comparingInt(
                s -> subcategoryOrder.getOrDefault(s.subcategory(), Integer.MAX_VALUE)))
            .toList();
    }

    public List<CategoryOptionResponse> getCategoryOptions() {
        Long userId = currentUserService.currentUserId();

        Map<String, Integer> categoryOrder = categoryOrderRepository.findAll()
            .stream()
            .collect(Collectors.toMap(
                order -> categoryOrderKey(order.getEnvironment(), order.getCategory()),
                CategoryOrder::getDisplayOrder
            ));

        // Environment order stays alphabetical (unchanged). Within an
        // environment, categories with an explicit order sort by it; a
        // category with no explicit order falls back to the alphabetical
        // position the query already returned it in (stable sort).
        return topicRepository.findDistinctEnvironmentCategories(userId)
            .stream()
            .sorted(Comparator
                .comparing((EnvironmentCategoryProjection option) -> option.getEnvironment())
                .thenComparingInt(option -> categoryOrder.getOrDefault(
                    categoryOrderKey(option.getEnvironment(), option.getCategory()), Integer.MAX_VALUE)))
            .map(option -> new CategoryOptionResponse(
                option.getEnvironment(),
                option.getCategory(),
                !starterPackService.isPublicCategory(userId, option.getCategory())
            ))
            .toList();
    }

    private static String categoryOrderKey(String environment, String category) {
        return environment.toLowerCase() + "::" + category.toLowerCase();
    }
}

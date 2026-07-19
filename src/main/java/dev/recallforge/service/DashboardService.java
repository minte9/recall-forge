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
import dev.recallforge.domain.SubcategoryOrder;
import dev.recallforge.dto.CategoryOptionResponse;
import dev.recallforge.dto.DashboardResponse;
import dev.recallforge.dto.KnowledgeAreaResponse;
import dev.recallforge.dto.MarkdownSummaryDto;
import dev.recallforge.dto.WeakAreaResponse;
import dev.recallforge.repository.KnowledgeAreaProjection;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.SubcategoryOrderRepository;
import dev.recallforge.repository.SubcategoryReviewCountProjection;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.security.CurrentUserService;

@Service
public class DashboardService {

    // A subcategory needs at least this many reviews before its average
    // memory score is trusted enough to call it "strong" or "weak" —
    // otherwise a single review is enough to move it off the shared
    // default score and make it look like a meaningful outlier.
    private static final long MIN_REVIEWS_FOR_KNOWLEDGE_INSIGHTS = 3;

    private final TopicRepository topicRepository;
    private final ReviewRepository reviewRepository;
    private final RewardService rewardService;
    private final CurrentUserService currentUserService;
    private final SubcategoryOrderRepository subcategoryOrderRepository;
    private final AppReviewLimitProperties reviewLimitProperties;

    public DashboardService(
        TopicRepository topicRepository,
        ReviewRepository reviewRepository,
        RewardService rewardService,
        CurrentUserService currentUserService,
        SubcategoryOrderRepository subcategoryOrderRepository,
        AppReviewLimitProperties reviewLimitProperties
    ) {
        this.topicRepository = topicRepository;
        this.reviewRepository = reviewRepository;
        this.rewardService = rewardService;
        this.currentUserService = currentUserService;
        this.subcategoryOrderRepository = subcategoryOrderRepository;
        this.reviewLimitProperties = reviewLimitProperties;
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


        List<WeakAreaResponse> weakAreas = topicRepository.findWeakAreas(userId, category, now)
            .stream()
            .limit(3)
            .map(area -> new WeakAreaResponse(
                area.getEnvironment(),
                area.getCategory(),
                area.getSubcategory(),
                area.getDueCount(),
                area.getAverageMemoryScore()
            ))
            .toList();

        Map<String, Long> reviewCountBySubcategory = reviewRepository.countReviewsBySubcategory(userId, category)
            .stream()
            .collect(Collectors.toMap(
                SubcategoryReviewCountProjection::getSubcategory,
                SubcategoryReviewCountProjection::getReviewCount
            ));

        List<KnowledgeAreaResponse> strongKnowledgeAreas = topicRepository.findStrongKnowledgeAreas(userId, category)
            .stream()
            .filter(area -> hasEnoughReviewsForInsights(area, reviewCountBySubcategory))
            .limit(2)
            .map(area -> new KnowledgeAreaResponse(
                area.getTitle(),
                area.getAverageMemoryScore()
            ))
            .toList();

        List<KnowledgeAreaResponse> weakKnowledgeAreas = topicRepository.findWeakKnowledgeAreas(userId, category)
            .stream()
            .filter(area -> hasEnoughReviewsForInsights(area, reviewCountBySubcategory))
            .limit(2)
            .map(area -> new KnowledgeAreaResponse(
                area.getTitle(),
                area.getAverageMemoryScore()
            ))
            .toList();

        Set<String> weakTitles = weakKnowledgeAreas.stream()
            .map(area -> area.title())
            .collect(Collectors.toSet());

        List<KnowledgeAreaResponse> strongOnly = strongKnowledgeAreas.stream()
            .filter(area -> !weakTitles.contains(area.title()))
            .toList();

        Set<String> strongTitles = strongKnowledgeAreas.stream()
            .map(area -> area.title())
            .collect(Collectors.toSet());

        List<KnowledgeAreaResponse> weakOnly = weakKnowledgeAreas.stream()
            .filter(area -> !strongTitles.contains(area.title()))
            .toList();

        // True only when there's a genuine strong/weak contrast between subcategories
        // that have each individually cleared MIN_REVIEWS_FOR_KNOWLEDGE_INSIGHTS —
        // strongKnowledgeAreas/weakKnowledgeAreas above are already filtered down to
        // those, so a single review to one otherwise-untouched subcategory can no
        // longer masquerade as a meaningful outlier.
        boolean showKnowledgeInsights =
            !strongOnly.isEmpty() && !weakOnly.isEmpty();

        int dailyReviewLimit = reviewLimitProperties.dailyLimit();
        long reviewsToday = reviewRepository.countByUserIdAndCategoryAndReviewedAtAfter(
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
            strongKnowledgeAreas,
            weakKnowledgeAreas,
            rewardService.getCurrentUserReward(category),
            showKnowledgeInsights,
            dailyReviewLimit,
            reviewsToday,
            todayReviewLimit,
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

        Map<String, Integer> subcategoryOrder = subcategoryOrderRepository.findByCategoryIgnoreCase(category)
            .stream()
            .collect(Collectors.toMap(SubcategoryOrder::getSubcategory, SubcategoryOrder::getDisplayOrder));

        // Stable sort: subcategories with no explicit order fall back to
        // the alphabetical order the query already returned them in.
        return summaries.stream()
            .sorted(Comparator.comparingInt(
                s -> subcategoryOrder.getOrDefault(s.subcategory(), Integer.MAX_VALUE)))
            .toList();
    }

    public List<CategoryOptionResponse> getCategoryOptions() {
        Long userId = currentUserService.currentUserId();

        return topicRepository.findDistinctEnvironmentCategories(userId)
            .stream()
            .map(option -> new CategoryOptionResponse(
                option.getEnvironment(),
                option.getCategory()
            ))
            .toList();
    }
}

package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.recallforge.config.AppReviewLimitProperties;
import dev.recallforge.domain.CategoryOrder;
import dev.recallforge.domain.SubcategoryOrder;
import dev.recallforge.dto.CategoryOptionResponse;
import dev.recallforge.dto.DashboardResponse;
import dev.recallforge.dto.KnowledgeAreaResponse;
import dev.recallforge.dto.MarkdownSummaryDto;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.dto.WeakAreaResponse;
import dev.recallforge.repository.CategoryOrderRepository;
import dev.recallforge.repository.EnvironmentCategoryProjection;
import dev.recallforge.repository.KnowledgeAreaProjection;
import dev.recallforge.repository.QuestionGenerationRepository;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.SubcategoryOrderRepository;
import dev.recallforge.repository.MarkdownFileCodeChallengeCountProjection;
import dev.recallforge.repository.SubcategoryReviewCountProjection;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.repository.WeakAreaProjection;
import dev.recallforge.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private QuestionGenerationRepository questionGenerationRepository;

    @Mock
    private RewardService rewardService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private SubcategoryOrderRepository subcategoryOrderRepository;

    @Mock
    private CategoryOrderRepository categoryOrderRepository;

    @Mock
    private AppReviewLimitProperties reviewLimitProperties;

    private DashboardService dashboardService;

    private void setUp() {
        dashboardService = new DashboardService(
            topicRepository, reviewRepository, questionGenerationRepository, rewardService, currentUserService,
            subcategoryOrderRepository, categoryOrderRepository, reviewLimitProperties
        );
        when(currentUserService.currentUserId()).thenReturn(1L);
    }

    private WeakAreaProjection weakArea(String environment, String category, String subcategory,
                                          long dueCount, double averageMemoryScore) {
        WeakAreaProjection projection = mock(WeakAreaProjection.class);
        when(projection.getEnvironment()).thenReturn(environment);
        when(projection.getCategory()).thenReturn(category);
        when(projection.getSubcategory()).thenReturn(subcategory);
        when(projection.getDueCount()).thenReturn(dueCount);
        when(projection.getAverageMemoryScore()).thenReturn(averageMemoryScore);
        return projection;
    }

    private KnowledgeAreaProjection knowledgeArea(String title, double averageMemoryScore) {
        KnowledgeAreaProjection projection = mock(KnowledgeAreaProjection.class);
        when(projection.getTitle()).thenReturn(title);
        when(projection.getAverageMemoryScore()).thenReturn(averageMemoryScore);
        return projection;
    }

    // Stubs only getTitle(): used where the item is expected to be filtered
    // out before getAverageMemoryScore() would ever be read, to avoid
    // Mockito's unnecessary-stubbing check flagging an unused stub.
    private KnowledgeAreaProjection knowledgeAreaTitleOnly(String title) {
        KnowledgeAreaProjection projection = mock(KnowledgeAreaProjection.class);
        when(projection.getTitle()).thenReturn(title);
        return projection;
    }

    private SubcategoryReviewCountProjection reviewCount(String subcategory, long reviewCount) {
        SubcategoryReviewCountProjection projection = mock(SubcategoryReviewCountProjection.class);
        when(projection.getSubcategory()).thenReturn(subcategory);
        when(projection.getReviewCount()).thenReturn(reviewCount);
        return projection;
    }

    private MarkdownFileCodeChallengeCountProjection codeChallengeCount(Long markdownFileId, long count) {
        MarkdownFileCodeChallengeCountProjection projection = mock(MarkdownFileCodeChallengeCountProjection.class);
        when(projection.getMarkdownFileId()).thenReturn(markdownFileId);
        when(projection.getCodeChallengeCount()).thenReturn(count);
        return projection;
    }

    private EnvironmentCategoryProjection environmentCategory(String environment, String category) {
        EnvironmentCategoryProjection projection = mock(EnvironmentCategoryProjection.class);
        when(projection.getEnvironment()).thenReturn(environment);
        when(projection.getCategory()).thenReturn(category);
        return projection;
    }

    @Test
    void getDashboard_limitsWeakAreasToTopThreeByDueCountAndMapsFieldsCorrectly() {
        setUp();
        // No SubcategoryOrder configured, so display order falls back to
        // alphabetical - subcategory names are distinct here so the
        // selection (top 3 by due count) can be checked without the display
        // reordering (covered separately below) affecting which 3 land.
        List<WeakAreaProjection> weakAreas = List.of(
            weakArea("Programming", "Java", "Core", 5L, 0.3),
            weakArea("Programming", "Python", "Testing", 4L, 0.4),
            weakArea("Math", "Algebra", "Basics", 3L, 0.5),
            mock(WeakAreaProjection.class) // beyond the top-3 limit; its fields are never read
        );
        when(topicRepository.findWeakAreas(eq(1L), eq("java"), any())).thenReturn(weakAreas);
        when(topicRepository.findStrongKnowledgeAreas(1L, "java")).thenReturn(List.of());
        when(topicRepository.findWeakKnowledgeAreas(1L, "java")).thenReturn(List.of());
        when(rewardService.getCurrentUserReward("java"))
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Java", 0L, 0L, 0));

        DashboardResponse dashboard = dashboardService.getDashboard("java");

        assertThat(dashboard.weakAreas()).containsExactlyInAnyOrder(
            new WeakAreaResponse("Programming", "Java", "Core", 5L, 0.3),
            new WeakAreaResponse("Programming", "Python", "Testing", 4L, 0.4),
            new WeakAreaResponse("Math", "Algebra", "Basics", 3L, 0.5)
        );
    }

    @Test
    void getDashboard_selectsBusiestThreeWeakAreasButDisplaysThemInSubcategoryOrder() {
        setUp();
        // Selection stays due-count-based (findWeakAreas already returns
        // busiest-first) - "Streams" is beyond the top-3 cutoff and is
        // correctly left out despite coming first in document order. Once
        // the busiest 3 are picked, they're displayed in subcategory order
        // (OOP, Generics, Collections) rather than by due count.
        List<WeakAreaProjection> weakAreas = List.of(
            weakArea("JVM", "Java", "Generics", 5L, 0.3),
            weakArea("JVM", "Java", "OOP", 4L, 0.4),
            weakArea("JVM", "Java", "Collections", 3L, 0.5),
            mock(WeakAreaProjection.class) // "Streams" - beyond the top-3 limit; its fields are never read
        );
        when(topicRepository.findWeakAreas(eq(1L), eq("java"), any())).thenReturn(weakAreas);
        when(subcategoryOrderRepository.findByCategoryIgnoreCase("java")).thenReturn(List.of(
            new SubcategoryOrder("Java", "Streams", 1),
            new SubcategoryOrder("Java", "OOP", 2),
            new SubcategoryOrder("Java", "Generics", 3),
            new SubcategoryOrder("Java", "Collections", 4)
        ));
        when(topicRepository.findStrongKnowledgeAreas(1L, "java")).thenReturn(List.of());
        when(topicRepository.findWeakKnowledgeAreas(1L, "java")).thenReturn(List.of());
        when(rewardService.getCurrentUserReward("java"))
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Java", 0L, 0L, 0));

        DashboardResponse dashboard = dashboardService.getDashboard("java");

        assertThat(dashboard.weakAreas()).extracting(WeakAreaResponse::subcategory)
            .containsExactly("OOP", "Generics", "Collections");
    }

    @Test
    void getDashboard_limitsStrongKnowledgeAreasToTopTwo() {
        setUp();
        when(topicRepository.findWeakAreas(eq(1L), eq("java"), any())).thenReturn(List.of());
        List<KnowledgeAreaProjection> strongAreas = List.of(
            knowledgeArea("Java, Core", 0.9),
            knowledgeArea("Python, Core", 0.85),
            mock(KnowledgeAreaProjection.class) // beyond the top-2 limit; its fields are never read
        );
        when(topicRepository.findStrongKnowledgeAreas(1L, "java")).thenReturn(strongAreas);
        when(topicRepository.findWeakKnowledgeAreas(1L, "java")).thenReturn(List.of());
        List<SubcategoryReviewCountProjection> reviewCounts = List.of(
            reviewCount("Java, Core", 5L),
            reviewCount("Python, Core", 5L)
        );
        when(reviewRepository.countReviewsBySubcategory(1L, "java")).thenReturn(reviewCounts);
        when(rewardService.getCurrentUserReward("java"))
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Java", 0L, 0L, 0));

        DashboardResponse dashboard = dashboardService.getDashboard("java");

        assertThat(dashboard.strongAreas()).containsExactly(
            new KnowledgeAreaResponse("Java, Core", 0.9),
            new KnowledgeAreaResponse("Python, Core", 0.85)
        );
    }

    @Test
    void getDashboard_limitsWeakKnowledgeAreasToTopTwo() {
        setUp();
        when(topicRepository.findWeakAreas(eq(1L), eq("java"), any())).thenReturn(List.of());
        when(topicRepository.findStrongKnowledgeAreas(1L, "java")).thenReturn(List.of());
        List<KnowledgeAreaProjection> weakKnowledgeAreas = List.of(
            knowledgeArea("Math, Basics", 0.2),
            knowledgeArea("Java, Core", 0.3),
            mock(KnowledgeAreaProjection.class) // beyond the top-2 limit; its fields are never read
        );
        when(topicRepository.findWeakKnowledgeAreas(1L, "java")).thenReturn(weakKnowledgeAreas);
        List<SubcategoryReviewCountProjection> reviewCounts = List.of(
            reviewCount("Math, Basics", 5L),
            reviewCount("Java, Core", 5L)
        );
        when(reviewRepository.countReviewsBySubcategory(1L, "java")).thenReturn(reviewCounts);
        when(rewardService.getCurrentUserReward("java"))
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Java", 0L, 0L, 0));

        DashboardResponse dashboard = dashboardService.getDashboard("java");

        assertThat(dashboard.weakKnowledgeAreas()).containsExactly(
            new KnowledgeAreaResponse("Math, Basics", 0.2),
            new KnowledgeAreaResponse("Java, Core", 0.3)
        );
    }

    @Test
    void getDashboard_subcategoryBelowMinReviews_excludedFromKnowledgeAreasAndInsightsStayHidden() {
        setUp();
        // Every topic starts at the same default memory score, so a single
        // review to an otherwise-untouched subcategory is enough to make it
        // rank as the top strong/weak subcategory purely by having moved off
        // the shared baseline - it shouldn't be trusted yet.
        when(topicRepository.findWeakAreas(eq(1L), eq("java"), any())).thenReturn(List.of());
        List<KnowledgeAreaProjection> strongAreas = List.of(knowledgeAreaTitleOnly("Collections"));
        List<KnowledgeAreaProjection> weakAreas = List.of(knowledgeAreaTitleOnly("Generics"));
        when(topicRepository.findStrongKnowledgeAreas(1L, "java")).thenReturn(strongAreas);
        when(topicRepository.findWeakKnowledgeAreas(1L, "java")).thenReturn(weakAreas);
        // "Generics" has zero reviews - absent from the map entirely.
        List<SubcategoryReviewCountProjection> reviewCounts = List.of(reviewCount("Collections", 1L));
        when(reviewRepository.countReviewsBySubcategory(1L, "java")).thenReturn(reviewCounts);
        when(rewardService.getCurrentUserReward("java"))
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Java", 0L, 0L, 0));

        DashboardResponse dashboard = dashboardService.getDashboard("java");

        assertThat(dashboard.strongAreas()).isEmpty();
        assertThat(dashboard.weakKnowledgeAreas()).isEmpty();
        assertThat(dashboard.showKnowledgeInsights()).isFalse();
    }

    @Test
    void getDashboard_distinctSubcategoriesEachClearingMinReviews_showKnowledgeInsightsBecomesTrue() {
        setUp();
        when(topicRepository.findWeakAreas(eq(1L), eq("java"), any())).thenReturn(List.of());
        List<KnowledgeAreaProjection> strongAreas = List.of(knowledgeArea("Collections", 0.9));
        List<KnowledgeAreaProjection> weakAreas = List.of(knowledgeArea("Generics", 0.3));
        when(topicRepository.findStrongKnowledgeAreas(1L, "java")).thenReturn(strongAreas);
        when(topicRepository.findWeakKnowledgeAreas(1L, "java")).thenReturn(weakAreas);
        List<SubcategoryReviewCountProjection> reviewCounts = List.of(
            reviewCount("Collections", 3L),
            reviewCount("Generics", 3L)
        );
        when(reviewRepository.countReviewsBySubcategory(1L, "java")).thenReturn(reviewCounts);
        when(rewardService.getCurrentUserReward("java"))
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Java", 0L, 0L, 0));

        DashboardResponse dashboard = dashboardService.getDashboard("java");

        assertThat(dashboard.strongAreas()).containsExactly(new KnowledgeAreaResponse("Collections", 0.9));
        assertThat(dashboard.weakKnowledgeAreas()).containsExactly(new KnowledgeAreaResponse("Generics", 0.3));
        assertThat(dashboard.showKnowledgeInsights()).isTrue();
    }

    @Test
    void getDashboard_subcategoryInBothTopTwoLists_excludedFromBothStrongAndWeak() {
        setUp();
        // With exactly 3 qualifying subcategories, the middle one (Exceptions)
        // is both the 2nd-highest and 2nd-lowest average, so it lands in both
        // top-2 lists. It's ambiguous, not distinctly strong or weak, so it
        // should be dropped from both rather than shown as both simultaneously.
        when(topicRepository.findWeakAreas(eq(1L), eq("java"), any())).thenReturn(List.of());
        List<KnowledgeAreaProjection> strongAreas = List.of(
            knowledgeArea("Collections", 0.9),
            knowledgeArea("Exceptions", 0.7)
        );
        List<KnowledgeAreaProjection> weakAreas = List.of(
            knowledgeArea("Basics", 0.3),
            knowledgeArea("Exceptions", 0.7)
        );
        when(topicRepository.findStrongKnowledgeAreas(1L, "java")).thenReturn(strongAreas);
        when(topicRepository.findWeakKnowledgeAreas(1L, "java")).thenReturn(weakAreas);
        List<SubcategoryReviewCountProjection> reviewCounts = List.of(
            reviewCount("Collections", 3L),
            reviewCount("Exceptions", 3L),
            reviewCount("Basics", 3L)
        );
        when(reviewRepository.countReviewsBySubcategory(1L, "java")).thenReturn(reviewCounts);
        when(rewardService.getCurrentUserReward("java"))
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Java", 0L, 0L, 0));

        DashboardResponse dashboard = dashboardService.getDashboard("java");

        assertThat(dashboard.strongAreas()).containsExactly(new KnowledgeAreaResponse("Collections", 0.9));
        assertThat(dashboard.weakKnowledgeAreas()).containsExactly(new KnowledgeAreaResponse("Basics", 0.3));
        assertThat(dashboard.showKnowledgeInsights()).isTrue();
    }

    @Test
    void getDashboard_passesThroughCountsAndRewardUnchanged() {
        setUp();
        when(topicRepository.countDueNowByUser(eq(1L), eq("java"), any())).thenReturn(10L);
        when(topicRepository.countDueBetweenByUser(eq(1L), eq("java"), any(), any())).thenReturn(5L, 8L);
        when(topicRepository.countByUser(1L, "java")).thenReturn(42L);
        when(topicRepository.findWeakAreas(eq(1L), eq("java"), any())).thenReturn(List.of());
        when(topicRepository.findStrongKnowledgeAreas(1L, "java")).thenReturn(List.of());
        when(topicRepository.findWeakKnowledgeAreas(1L, "java")).thenReturn(List.of());
        RewardResponse reward = new RewardResponse(250, 3, "Apprentice", 50, 100, 4, 0, "Java", 5L, 10L, 50);
        when(rewardService.getCurrentUserReward("java")).thenReturn(reward);

        DashboardResponse dashboard = dashboardService.getDashboard("java");

        assertThat(dashboard.dueNow()).isEqualTo(10L);
        assertThat(dashboard.dueTomorrow()).isEqualTo(5L);
        assertThat(dashboard.dueThisWeek()).isEqualTo(8L);
        assertThat(dashboard.totalTopics()).isEqualTo(42L);
        assertThat(dashboard.reward()).isEqualTo(reward);
    }

    @Test
    void getDashboard_todayReviewLimit_usesCategoryScopedReviewCountNotGlobal() {
        setUp();
        when(topicRepository.countDueNowByUser(eq(1L), eq("python"), any())).thenReturn(13L);
        when(topicRepository.countDueBetweenByUser(eq(1L), eq("python"), any(), any())).thenReturn(0L, 0L);
        when(topicRepository.countByUser(1L, "python")).thenReturn(14L);
        when(topicRepository.findWeakAreas(eq(1L), eq("python"), any())).thenReturn(List.of());
        when(topicRepository.findStrongKnowledgeAreas(1L, "python")).thenReturn(List.of());
        when(topicRepository.findWeakKnowledgeAreas(1L, "python")).thenReturn(List.of());
        when(rewardService.getCurrentUserReward("python"))
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Python", 0L, 0L, 0));
        when(reviewLimitProperties.dailyLimit()).thenReturn(20);
        // Only 1 of the day's reviews was in "python"; a same-day review in another
        // category (e.g. "java") must not inflate this category's count or limit.
        when(reviewRepository.countByUserIdAndCategoryAndReviewedAtAfter(eq(1L), eq("python"), any()))
            .thenReturn(1L);

        DashboardResponse dashboard = dashboardService.getDashboard("python");

        assertThat(dashboard.reviewsToday()).isEqualTo(1L);
        assertThat(dashboard.todayReviewLimit()).isEqualTo(14L);
        verify(reviewRepository).countByUserIdAndCategoryAndReviewedAtAfter(eq(1L), eq("python"), any());
    }

    @Test
    void getDashboard_questionsToday_usesCategoryScopedQuestionCountNotGlobal() {
        setUp();
        when(topicRepository.findWeakAreas(eq(1L), eq("python"), any())).thenReturn(List.of());
        when(topicRepository.findStrongKnowledgeAreas(1L, "python")).thenReturn(List.of());
        when(topicRepository.findWeakKnowledgeAreas(1L, "python")).thenReturn(List.of());
        when(rewardService.getCurrentUserReward("python"))
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Python", 0L, 0L, 0));
        // Only 3 of the day's generated questions were in "python"; a same-day
        // question in another category (e.g. "java") must not inflate this
        // category's count.
        when(questionGenerationRepository.countByUserIdAndCategoryAndGeneratedAtAfter(eq(1L), eq("python"), any()))
            .thenReturn(3L);

        DashboardResponse dashboard = dashboardService.getDashboard("python");

        assertThat(dashboard.questionsToday()).isEqualTo(3L);
        verify(questionGenerationRepository).countByUserIdAndCategoryAndGeneratedAtAfter(eq(1L), eq("python"), any());
    }

    @Test
    void getDashboard_emptyRepositoryResults_produceEmptyListsNotErrors() {
        setUp();
        when(topicRepository.findWeakAreas(eq(1L), eq("java"), any())).thenReturn(List.of());
        when(topicRepository.findStrongKnowledgeAreas(1L, "java")).thenReturn(List.of());
        when(topicRepository.findWeakKnowledgeAreas(1L, "java")).thenReturn(List.of());
        when(rewardService.getCurrentUserReward("java"))
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Java", 0L, 0L, 0));

        DashboardResponse dashboard = dashboardService.getDashboard("java");

        assertThat(dashboard.weakAreas()).isEmpty();
        assertThat(dashboard.strongAreas()).isEmpty();
        assertThat(dashboard.weakKnowledgeAreas()).isEmpty();
    }

    @Test
    void getMarkdownSummaries_delegatesToRepositoryWithCurrentUserId() {
        setUp();
        List<MarkdownSummaryDto> summaries = List.of(
            new MarkdownSummaryDto(1L, "Programming", "Java", "Core", "Basics", 5L, 2L, 0.6)
        );
        when(topicRepository.findMarkdownSummaries(eq(1L), eq("java"), any())).thenReturn(summaries);

        List<MarkdownSummaryDto> result = dashboardService.getMarkdownSummaries("java");

        assertThat(result).isEqualTo(summaries);
    }

    @Test
    void getMarkdownSummaries_mergesCodeChallengeCountsFromReviewRepository() {
        setUp();
        MarkdownSummaryDto fileWithChallenge =
            new MarkdownSummaryDto(1L, "Programming", "Java", "Core", "Basics", 5L, 2L, 0.6);
        MarkdownSummaryDto fileWithoutChallenge =
            new MarkdownSummaryDto(2L, "Programming", "Java", "Core", "OOP", 3L, 1L, 0.7);
        when(topicRepository.findMarkdownSummaries(eq(1L), eq("java"), any()))
            .thenReturn(List.of(fileWithChallenge, fileWithoutChallenge));
        List<MarkdownFileCodeChallengeCountProjection> codeChallengeCounts = List.of(codeChallengeCount(1L, 2L));
        when(reviewRepository.countCodeChallengesByMarkdownFile(1L, "java")).thenReturn(codeChallengeCounts);

        List<MarkdownSummaryDto> result = dashboardService.getMarkdownSummaries("java");

        assertThat(result).extracting(MarkdownSummaryDto::markdownFileId, MarkdownSummaryDto::codeChallengeCount)
            .containsExactly(tuple(1L, 2L), tuple(2L, 0L));
    }

    @Test
    void getMarkdownSummaries_reordersSubcategoriesByConfiguredDisplayOrder() {
        setUp();
        // Repository already returns them alphabetically (Collections, Generics, OOP)
        MarkdownSummaryDto collectionsFile =
            new MarkdownSummaryDto(1L, "JVM", "Java", "Collections", "Sets", 3L, 1L, 0.6);
        MarkdownSummaryDto genericsFile =
            new MarkdownSummaryDto(2L, "JVM", "Java", "Generics", "Basics", 3L, 1L, 0.6);
        MarkdownSummaryDto oopFile =
            new MarkdownSummaryDto(3L, "JVM", "Java", "OOP", "Inheritance", 3L, 1L, 0.6);
        when(topicRepository.findMarkdownSummaries(eq(1L), eq("java"), any()))
            .thenReturn(List.of(collectionsFile, genericsFile, oopFile));
        when(subcategoryOrderRepository.findByCategoryIgnoreCase("java")).thenReturn(List.of(
            new SubcategoryOrder("Java", "OOP", 1),
            new SubcategoryOrder("Java", "Generics", 2),
            new SubcategoryOrder("Java", "Collections", 3)
        ));

        List<MarkdownSummaryDto> result = dashboardService.getMarkdownSummaries("java");

        assertThat(result).containsExactly(oopFile, genericsFile, collectionsFile);
    }

    @Test
    void getMarkdownSummaries_subcategoryWithNoConfiguredOrder_sortsAfterConfiguredOnesKeepingAlphabeticalFallback() {
        setUp();
        MarkdownSummaryDto oopFile = new MarkdownSummaryDto(1L, "JVM", "Java", "OOP", "Inheritance", 3L, 1L, 0.6);
        MarkdownSummaryDto streamsFile = new MarkdownSummaryDto(2L, "JVM", "Java", "Streams", "Creation", 3L, 1L, 0.6);
        MarkdownSummaryDto versionsFile = new MarkdownSummaryDto(3L, "JVM", "Java", "Versions", "Java 8", 3L, 1L, 0.6);
        // repository already returns alphabetical order for the unconfigured ones (Streams, Versions)
        when(topicRepository.findMarkdownSummaries(eq(1L), eq("java"), any()))
            .thenReturn(List.of(streamsFile, versionsFile, oopFile));
        when(subcategoryOrderRepository.findByCategoryIgnoreCase("java")).thenReturn(List.of(
            new SubcategoryOrder("Java", "OOP", 1)
        ));

        List<MarkdownSummaryDto> result = dashboardService.getMarkdownSummaries("java");

        assertThat(result).containsExactly(oopFile, streamsFile, versionsFile);
    }

    @Test
    void getCategoryOptions_delegatesToRepositoryForCurrentUserAndMapsFields() {
        setUp();
        List<EnvironmentCategoryProjection> options = List.of(
            environmentCategory("JVM", "Java"),
            environmentCategory("JVM", "Spring"),
            environmentCategory("PVM", "Python")
        );
        when(topicRepository.findDistinctEnvironmentCategories(1L)).thenReturn(options);

        List<CategoryOptionResponse> result = dashboardService.getCategoryOptions();

        assertThat(result).containsExactly(
            new CategoryOptionResponse("JVM", "Java"),
            new CategoryOptionResponse("JVM", "Spring"),
            new CategoryOptionResponse("PVM", "Python")
        );
    }

    @Test
    void getCategoryOptions_repositoryReturnsEmptyList_returnsEmptyList() {
        setUp();
        when(topicRepository.findDistinctEnvironmentCategories(1L)).thenReturn(List.of());

        List<CategoryOptionResponse> result = dashboardService.getCategoryOptions();

        assertThat(result).isEmpty();
    }

    @Test
    void getCategoryOptions_reordersCategoriesWithinEnvironmentByConfiguredDisplayOrder() {
        setUp();
        // Repository already returns them alphabetically (Antipatterns, SQL) within DB
        List<EnvironmentCategoryProjection> options = List.of(
            environmentCategory("DB", "Antipatterns"),
            environmentCategory("DB", "SQL")
        );
        when(topicRepository.findDistinctEnvironmentCategories(1L)).thenReturn(options);
        when(categoryOrderRepository.findAll()).thenReturn(List.of(
            new CategoryOrder("DB", "SQL", 1),
            new CategoryOrder("DB", "Antipatterns", 2)
        ));

        List<CategoryOptionResponse> result = dashboardService.getCategoryOptions();

        assertThat(result).containsExactly(
            new CategoryOptionResponse("DB", "SQL"),
            new CategoryOptionResponse("DB", "Antipatterns")
        );
    }

    @Test
    void getCategoryOptions_categoryWithNoConfiguredOrder_sortsAfterConfiguredOnesKeepingAlphabeticalFallback() {
        setUp();
        // repository already returns alphabetical order for the unconfigured ones (Python, Spring)
        List<EnvironmentCategoryProjection> options = List.of(
            environmentCategory("JVM", "Python"),
            environmentCategory("JVM", "Spring"),
            environmentCategory("JVM", "Java")
        );
        when(topicRepository.findDistinctEnvironmentCategories(1L)).thenReturn(options);
        when(categoryOrderRepository.findAll()).thenReturn(List.of(
            new CategoryOrder("JVM", "Java", 1)
        ));

        List<CategoryOptionResponse> result = dashboardService.getCategoryOptions();

        assertThat(result).containsExactly(
            new CategoryOptionResponse("JVM", "Java"),
            new CategoryOptionResponse("JVM", "Python"),
            new CategoryOptionResponse("JVM", "Spring")
        );
    }

    @Test
    void getCategoryOptions_configuredOrderDoesNotLeakAcrossEnvironments() {
        setUp();
        // A category ranked #1 in its own environment must not jump ahead of
        // an unrelated environment's categories - environment order stays
        // alphabetical regardless of per-category display order values.
        List<EnvironmentCategoryProjection> options = List.of(
            environmentCategory("DB", "Antipatterns"),
            environmentCategory("DB", "SQL"),
            environmentCategory("JVM", "Java")
        );
        when(topicRepository.findDistinctEnvironmentCategories(1L)).thenReturn(options);
        when(categoryOrderRepository.findAll()).thenReturn(List.of(
            new CategoryOrder("JVM", "Java", 1),
            new CategoryOrder("DB", "SQL", 1),
            new CategoryOrder("DB", "Antipatterns", 2)
        ));

        List<CategoryOptionResponse> result = dashboardService.getCategoryOptions();

        assertThat(result).containsExactly(
            new CategoryOptionResponse("DB", "SQL"),
            new CategoryOptionResponse("DB", "Antipatterns"),
            new CategoryOptionResponse("JVM", "Java")
        );
    }
}

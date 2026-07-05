package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.recallforge.dto.DashboardResponse;
import dev.recallforge.dto.KnowledgeAreaResponse;
import dev.recallforge.dto.MarkdownSummaryDto;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.dto.WeakAreaResponse;
import dev.recallforge.repository.KnowledgeAreaProjection;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.repository.WeakAreaProjection;
import dev.recallforge.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private RewardService rewardService;

    @Mock
    private CurrentUserService currentUserService;

    private DashboardService dashboardService;

    private void setUp() {
        dashboardService = new DashboardService(topicRepository, rewardService, currentUserService);
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

    @Test
    void getDashboard_limitsWeakAreasToTopThreeAndMapsFieldsCorrectly() {
        setUp();
        List<WeakAreaProjection> weakAreas = List.of(
            weakArea("Programming", "Java", "Core", 5L, 0.3),
            weakArea("Programming", "Python", "Core", 4L, 0.4),
            weakArea("Math", "Algebra", "Basics", 3L, 0.5),
            mock(WeakAreaProjection.class) // beyond the top-3 limit; its fields are never read
        );
        when(topicRepository.findWeakAreas(eq(1L), any())).thenReturn(weakAreas);
        when(topicRepository.findStrongKnowledgeAreas(1L)).thenReturn(List.of());
        when(topicRepository.findWeakKnowledgeAreas(1L)).thenReturn(List.of());
        when(rewardService.getCurrentUserReward())
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Java", 0L, 0L, 0));

        DashboardResponse dashboard = dashboardService.getDashboard();

        assertThat(dashboard.weakAreas()).hasSize(3);
        assertThat(dashboard.weakAreas().get(0))
            .isEqualTo(new WeakAreaResponse("Programming", "Java", "Core", 5L, 0.3));
        assertThat(dashboard.weakAreas().get(2))
            .isEqualTo(new WeakAreaResponse("Math", "Algebra", "Basics", 3L, 0.5));
    }

    @Test
    void getDashboard_limitsStrongKnowledgeAreasToTopTwo() {
        setUp();
        when(topicRepository.findWeakAreas(eq(1L), any())).thenReturn(List.of());
        List<KnowledgeAreaProjection> strongAreas = List.of(
            knowledgeArea("Java, Core", 0.9),
            knowledgeArea("Python, Core", 0.85),
            mock(KnowledgeAreaProjection.class) // beyond the top-2 limit; its fields are never read
        );
        when(topicRepository.findStrongKnowledgeAreas(1L)).thenReturn(strongAreas);
        when(topicRepository.findWeakKnowledgeAreas(1L)).thenReturn(List.of());
        when(rewardService.getCurrentUserReward())
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Java", 0L, 0L, 0));

        DashboardResponse dashboard = dashboardService.getDashboard();

        assertThat(dashboard.strongAreas()).containsExactly(
            new KnowledgeAreaResponse("Java, Core", 0.9),
            new KnowledgeAreaResponse("Python, Core", 0.85)
        );
    }

    @Test
    void getDashboard_limitsWeakKnowledgeAreasToTopTwo() {
        setUp();
        when(topicRepository.findWeakAreas(eq(1L), any())).thenReturn(List.of());
        when(topicRepository.findStrongKnowledgeAreas(1L)).thenReturn(List.of());
        List<KnowledgeAreaProjection> weakKnowledgeAreas = List.of(
            knowledgeArea("Math, Basics", 0.2),
            knowledgeArea("Java, Core", 0.3),
            mock(KnowledgeAreaProjection.class) // beyond the top-2 limit; its fields are never read
        );
        when(topicRepository.findWeakKnowledgeAreas(1L)).thenReturn(weakKnowledgeAreas);
        when(rewardService.getCurrentUserReward())
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Java", 0L, 0L, 0));

        DashboardResponse dashboard = dashboardService.getDashboard();

        assertThat(dashboard.weakKnowledgeAreas()).containsExactly(
            new KnowledgeAreaResponse("Math, Basics", 0.2),
            new KnowledgeAreaResponse("Java, Core", 0.3)
        );
    }

    @Test
    void getDashboard_passesThroughCountsAndRewardUnchanged() {
        setUp();
        when(topicRepository.countDueNowByUser(eq(1L), any())).thenReturn(10L);
        when(topicRepository.countDueBetweenByUser(eq(1L), any(), any())).thenReturn(5L, 8L);
        when(topicRepository.countByUser(1L)).thenReturn(42L);
        when(topicRepository.findWeakAreas(eq(1L), any())).thenReturn(List.of());
        when(topicRepository.findStrongKnowledgeAreas(1L)).thenReturn(List.of());
        when(topicRepository.findWeakKnowledgeAreas(1L)).thenReturn(List.of());
        RewardResponse reward = new RewardResponse(250, 3, "Apprentice", 50, 100, 4, 0, "Java", 5L, 10L, 50);
        when(rewardService.getCurrentUserReward()).thenReturn(reward);

        DashboardResponse dashboard = dashboardService.getDashboard();

        assertThat(dashboard.dueNow()).isEqualTo(10L);
        assertThat(dashboard.dueTomorrow()).isEqualTo(5L);
        assertThat(dashboard.dueThisWeek()).isEqualTo(8L);
        assertThat(dashboard.totalTopics()).isEqualTo(42L);
        assertThat(dashboard.reward()).isEqualTo(reward);
    }

    @Test
    void getDashboard_emptyRepositoryResults_produceEmptyListsNotErrors() {
        setUp();
        when(topicRepository.findWeakAreas(eq(1L), any())).thenReturn(List.of());
        when(topicRepository.findStrongKnowledgeAreas(1L)).thenReturn(List.of());
        when(topicRepository.findWeakKnowledgeAreas(1L)).thenReturn(List.of());
        when(rewardService.getCurrentUserReward())
            .thenReturn(new RewardResponse(0, 1, "Learner", 0, 100, 0, 0, "Java", 0L, 0L, 0));

        DashboardResponse dashboard = dashboardService.getDashboard();

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
        when(topicRepository.findMarkdownSummaries(eq(1L), any())).thenReturn(summaries);

        List<MarkdownSummaryDto> result = dashboardService.getMarkdownSummaries();

        assertThat(result).isEqualTo(summaries);
    }
}

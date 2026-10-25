package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.repository.MarkdownFileRepository;
import dev.recallforge.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class StarterPackServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private MarkdownFileRepository markdownFileRepository;

    @Mock
    private MarkdownFileService markdownFileService;

    @Mock
    private RewardService rewardService;

    private StarterPackService starterPackService;

    private void setUp() {
        starterPackService = new StarterPackService(
            currentUserService, markdownFileRepository, markdownFileService, rewardService
        );
    }

    private static RewardResponse rewardWithReviewed(String category, int reviewedPercent) {
        return new RewardResponse(0, 0, false, 0, 0, category, 0, reviewedPercent, 0, 0, 0, 0L, 0, 0L, 0, 0L, 0L, 0L, false);
    }

    private static MarkdownFile templateFile(String content, Integer displayOrder) {
        UserAccount templateUser = new UserAccount();
        MarkdownFile markdownFile = new MarkdownFile(
            "notes.md", content, "hash-" + content, "Programming", "Java", "Core", "Java Basics", templateUser
        );
        markdownFile.updateDisplayOrder(displayOrder);
        return markdownFile;
    }

    @Test
    void importStarterPack_templateUser_throwsIllegalStateException() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);

        assertThatThrownBy(() -> starterPackService.importStarterPack("Java"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Template user cannot import starter packs.");

        verify(markdownFileRepository, never()).findByUserIdAndCategory(eq(1L), eq("Java"));
    }

    @Test
    void importStarterPack_publicCategoryWithNoTemplateFiles_throwsIllegalArgumentException() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(2L);
        when(markdownFileRepository.findByUserIdAndCategory(1L, "Python")).thenReturn(List.of());

        assertThatThrownBy(() -> starterPackService.importStarterPack("Python"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Starter pack not found: Python");
    }

    @Test
    void importStarterPack_categoryNotPublic_throwsIllegalArgumentExceptionWithoutQueryingRepository() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(2L);

        assertThatThrownBy(() -> starterPackService.importStarterPack("Unknown"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Starter pack not found: Unknown");

        verify(markdownFileRepository, never()).findByUserIdAndCategory(eq(1L), eq("Unknown"));
    }

    @Test
    void importStarterPack_forwardsEachTemplateFilesContentAndDisplayOrder() throws Exception {
        setUp();
        MarkdownFile first = templateFile("## Topic 1\ncontent", 1);
        MarkdownFile second = templateFile("## Topic 2\ncontent", 2);
        MarkdownFile withoutOrder = templateFile("## Topic 3\ncontent", null);

        when(currentUserService.currentUserId()).thenReturn(2L);
        when(markdownFileRepository.findByUserIdAndCategory(1L, "Java"))
            .thenReturn(List.of(first, second, withoutOrder));

        starterPackService.importStarterPack("Java");

        verify(markdownFileService).importMarkdown("## Topic 1\ncontent", 1, first);
        verify(markdownFileService).importMarkdown("## Topic 2\ncontent", 2, second);
        verify(markdownFileService).importMarkdown("## Topic 3\ncontent", null, withoutOrder);
    }

    @Test
    void isPublicCategory_templateUserPublicCategoryCaseInsensitive_returnsTrue() {
        setUp();

        assertThat(starterPackService.isPublicCategory(1L, "java")).isTrue();
    }

    @Test
    void isPublicCategory_templateUserNonPublicCategory_returnsFalse() {
        setUp();

        assertThat(starterPackService.isPublicCategory(1L, "Rust")).isFalse();
    }

    @Test
    void isPublicCategory_templateUserRowTwoCategoryCaseInsensitive_returnsTrue() {
        setUp();

        assertThat(starterPackService.isPublicCategory(1L, "spring")).isTrue();
        assertThat(starterPackService.isPublicCategory(1L, "docker")).isTrue();
        assertThat(starterPackService.isPublicCategory(1L, "typescript")).isTrue();
    }

    @Test
    void isPublicCategory_templateUserKotlinOracleNodeOAuth2_returnsFalse() {
        setUp();

        assertThat(starterPackService.isPublicCategory(1L, "kotlin")).isFalse();
        assertThat(starterPackService.isPublicCategory(1L, "oracle")).isFalse();
        assertThat(starterPackService.isPublicCategory(1L, "node")).isFalse();
        assertThat(starterPackService.isPublicCategory(1L, "oauth2")).isFalse();
    }

    @Test
    void isPublicCategory_templateUserMongoDb_returnsFalse() {
        setUp();

        // Private for now, to keep the Starter Pack panel at 4 rows.
        assertThat(starterPackService.isPublicCategory(1L, "MongoDB")).isFalse();
    }

    @Test
    void isPublicCategory_templateUserFullCatalogAdditionsCaseInsensitive_returnsTrue() {
        setUp();

        assertThat(starterPackService.isPublicCategory(1L, "postgresql")).isTrue();
        assertThat(starterPackService.isPublicCategory(1L, "https")).isTrue();
        assertThat(starterPackService.isPublicCategory(1L, "mcp")).isTrue();
    }

    @Test
    void isPublicCategory_templateUserMicroservicesMySqlVueJsFastApi_returnsFalse() {
        setUp();

        assertThat(starterPackService.isPublicCategory(1L, "microservices")).isFalse();
        assertThat(starterPackService.isPublicCategory(1L, "mysql")).isFalse();
        assertThat(starterPackService.isPublicCategory(1L, "vue.js")).isFalse();
        assertThat(starterPackService.isPublicCategory(1L, "fastapi")).isFalse();
    }

    @Test
    void isPublicCategory_nonTemplateUserSameCategoryName_returnsFalse() {
        setUp();

        assertThat(starterPackService.isPublicCategory(2L, "Java")).isFalse();
    }

    @Test
    void getUnlockStatus_prerequisiteAtOrAboveThreshold_isUnlocked() {
        setUp();
        when(rewardService.getCurrentUserReward(anyString())).thenReturn(rewardWithReviewed("x", 0));
        when(rewardService.getCurrentUserReward("Java")).thenReturn(rewardWithReviewed("Java", 30));

        assertThat(starterPackService.getUnlockStatus()).containsEntry("Spring", true);
    }

    @Test
    void getUnlockStatus_prerequisiteBelowThreshold_isLocked() {
        setUp();
        when(rewardService.getCurrentUserReward(anyString())).thenReturn(rewardWithReviewed("x", 0));
        when(rewardService.getCurrentUserReward("Java")).thenReturn(rewardWithReviewed("Java", 29));

        assertThat(starterPackService.getUnlockStatus()).containsEntry("Spring", false);
    }

    // The gate follows reviewedPercent, not the stricter masteryPercent
    // (which also needs a 3-review streak).
    @Test
    void getUnlockStatus_ignoresMasteryPercent() {
        setUp();
        when(rewardService.getCurrentUserReward(anyString())).thenReturn(rewardWithReviewed("x", 0));
        when(rewardService.getCurrentUserReward("Java")).thenReturn(
            new RewardResponse(0, 0, false, 0, 0, "Java", 0, 29, 0, 0, 100, 0L, 0, 0L, 0, 0L, 0L, 0L, false));

        assertThat(starterPackService.getUnlockStatus()).containsEntry("Spring", false);
    }

    @Test
    void getUnlockStatus_coversEveryGatedCategory() {
        setUp();
        when(rewardService.getCurrentUserReward(anyString()))
            .thenReturn(rewardWithReviewed("x", 0));

        assertThat(starterPackService.getUnlockStatus())
            .containsOnlyKeys(
                "Spring", "Algorithms", "Docker", "TypeScript", "Angular",
                "PostgreSQL", "GraphQL", "MCP", "HTTPS"
            );
    }

    @Test
    void getUnlockStatus_allDependentsOfALevelOneCategoryUnlockTogetherOnItsReviewedPercentAlone() {
        setUp();
        // Security reviewed, HTTPS never touched (0% reviewed) - it still
        // unlocks, since it gates directly on Security alone.
        when(rewardService.getCurrentUserReward(anyString())).thenReturn(rewardWithReviewed("x", 0));
        when(rewardService.getCurrentUserReward("Security")).thenReturn(rewardWithReviewed("Security", 30));

        assertThat(starterPackService.getUnlockStatus())
            .containsEntry("HTTPS", true)
            .containsEntry("Spring", false);
    }

    @Test
    void getReviewedPercentages_coversEveryPublicCategoryAndForwardsPercent() {
        setUp();
        when(rewardService.getCurrentUserReward(anyString())).thenReturn(rewardWithReviewed("x", 0));
        when(rewardService.getCurrentUserReward("Java")).thenReturn(rewardWithReviewed("Java", 73));

        assertThat(starterPackService.getReviewedPercentages())
            .containsOnlyKeys(
                "Java", "Python", "Git", "SQL", "Javascript",
                "Spring", "Algorithms", "Docker", "TypeScript", "Angular",
                "Security", "PostgreSQL", "REST", "GraphQL", "MCP", "HTTPS"
            )
            .containsEntry("Java", 73);
    }
}

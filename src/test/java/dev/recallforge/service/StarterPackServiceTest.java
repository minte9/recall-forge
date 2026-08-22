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

    private static RewardResponse rewardWithMastery(String category, int masteryPercent) {
        return new RewardResponse(0, 0, false, 0, 0, category, 0, 0, masteryPercent);
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
        MarkdownFile first = templateFile("### Topic 1\ncontent", 1);
        MarkdownFile second = templateFile("### Topic 2\ncontent", 2);
        MarkdownFile withoutOrder = templateFile("### Topic 3\ncontent", null);

        when(currentUserService.currentUserId()).thenReturn(2L);
        when(markdownFileRepository.findByUserIdAndCategory(1L, "Java"))
            .thenReturn(List.of(first, second, withoutOrder));

        starterPackService.importStarterPack("Java");

        verify(markdownFileService).importMarkdown("### Topic 1\ncontent", 1);
        verify(markdownFileService).importMarkdown("### Topic 2\ncontent", 2);
        verify(markdownFileService).importMarkdown("### Topic 3\ncontent", null);
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
        assertThat(starterPackService.isPublicCategory(1L, "fastapi")).isTrue();
        assertThat(starterPackService.isPublicCategory(1L, "docker")).isTrue();
        assertThat(starterPackService.isPublicCategory(1L, "mysql")).isTrue();
        assertThat(starterPackService.isPublicCategory(1L, "node.js")).isTrue();
    }

    @Test
    void isPublicCategory_templateUserRowThreeCategoryCaseInsensitive_returnsTrue() {
        setUp();

        assertThat(starterPackService.isPublicCategory(1L, "kotlin")).isTrue();
        assertThat(starterPackService.isPublicCategory(1L, "mongodb")).isTrue();
        assertThat(starterPackService.isPublicCategory(1L, "typescript")).isTrue();
    }

    @Test
    void isPublicCategory_nonTemplateUserSameCategoryName_returnsFalse() {
        setUp();

        assertThat(starterPackService.isPublicCategory(2L, "Java")).isFalse();
    }

    @Test
    void getUnlockStatus_prerequisiteAtOrAboveThreshold_isUnlocked() {
        setUp();
        when(rewardService.getCurrentUserReward(anyString())).thenReturn(rewardWithMastery("x", 0));
        when(rewardService.getCurrentUserReward("Java")).thenReturn(rewardWithMastery("Java", 50));

        assertThat(starterPackService.getUnlockStatus()).containsEntry("Spring", true);
    }

    @Test
    void getUnlockStatus_prerequisiteBelowThreshold_isLocked() {
        setUp();
        when(rewardService.getCurrentUserReward(anyString())).thenReturn(rewardWithMastery("x", 0));
        when(rewardService.getCurrentUserReward("Java")).thenReturn(rewardWithMastery("Java", 49));

        assertThat(starterPackService.getUnlockStatus()).containsEntry("Spring", false);
    }

    @Test
    void getUnlockStatus_coversEveryGatedCategory() {
        setUp();
        when(rewardService.getCurrentUserReward(anyString()))
            .thenReturn(rewardWithMastery("x", 0));

        assertThat(starterPackService.getUnlockStatus())
            .containsOnlyKeys(
                "Spring", "FastAPI", "Docker", "MySQL", "Node.js",
                "Kotlin", "MongoDB", "TypeScript"
            );
    }

    @Test
    void getUnlockStatus_rowThreeGatesOnRowTwoNotRowOne() {
        setUp();
        // Row 1 (Java) mastered, but row 2 (Spring) isn't yet - row 3 (Kotlin)
        // should stay locked on Spring's mastery, not Java's.
        when(rewardService.getCurrentUserReward(anyString())).thenReturn(rewardWithMastery("x", 0));
        when(rewardService.getCurrentUserReward("Java")).thenReturn(rewardWithMastery("Java", 100));
        when(rewardService.getCurrentUserReward("Spring")).thenReturn(rewardWithMastery("Spring", 49));

        assertThat(starterPackService.getUnlockStatus()).containsEntry("Kotlin", false);

        when(rewardService.getCurrentUserReward("Spring")).thenReturn(rewardWithMastery("Spring", 50));

        assertThat(starterPackService.getUnlockStatus()).containsEntry("Kotlin", true);
    }

    @Test
    void getMasteryPercentages_coversEveryPublicCategoryAndForwardsPercent() {
        setUp();
        when(rewardService.getCurrentUserReward(anyString())).thenReturn(rewardWithMastery("x", 0));
        when(rewardService.getCurrentUserReward("Java")).thenReturn(rewardWithMastery("Java", 73));

        assertThat(starterPackService.getMasteryPercentages())
            .containsOnlyKeys(
                "Java", "Python", "Git", "SQL", "Javascript",
                "Spring", "FastAPI", "Docker", "MySQL", "Node.js",
                "Kotlin", "MongoDB", "TypeScript"
            )
            .containsEntry("Java", 73);
    }
}

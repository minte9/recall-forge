package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import dev.recallforge.domain.Topic;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.repository.SubcategoryOrderRepository;
import dev.recallforge.repository.TopicRepository;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private SubcategoryOrderRepository subcategoryOrderRepository;

    private TopicService topicService;

    private void setUp() {
        topicService = new TopicService(topicRepository, subcategoryOrderRepository);
    }

    private Topic topicWithMemoryScore(double memoryScore) {
        Topic topic = new Topic(
            "General", "Uncategorized", "Uncategorized", "File",
            "Title", "Content", null, new UserAccount()
        );
        topic.updateMemory(memoryScore, LocalDateTime.now());
        return topic;
    }

    private Topic topicWithTitle(String title) {
        return new Topic(
            "General", "Uncategorized", "Uncategorized", "File",
            title, "Content", null, new UserAccount()
        );
    }

    private Topic topicWithSubcategoryAndTitle(String subcategory, String title) {
        return new Topic(
            "General", "Uncategorized", subcategory, "File",
            title, "Content", null, new UserAccount()
        );
    }

    private Topic topicWithTitleAndMemoryScore(String title, double memoryScore) {
        Topic topic = topicWithTitle(title);
        topic.updateMemory(memoryScore, LocalDateTime.now());
        return topic;
    }

    @Test
    void getAllTopics_sortsResultsByMemoryScoreAscending() {
        setUp();
        Topic low = topicWithMemoryScore(0.2);
        Topic mid = topicWithMemoryScore(0.5);
        Topic high = topicWithMemoryScore(0.9);
        when(topicRepository.findByUserId(1L)).thenReturn(List.of(high, low, mid));

        List<Topic> result = topicService.getAllTopics(1L);

        assertThat(result).containsExactly(low, mid, high);
    }

    @Test
    void getTopic_returnsTopicWhenFoundForUser() {
        setUp();
        Topic topic = topicWithMemoryScore(0.5);
        when(topicRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(topic));

        assertThat(topicService.getTopic(5L, 1L)).isSameAs(topic);
    }

    @Test
    void getTopic_throwsIllegalArgumentExceptionMentioningIdWhenNotFound() {
        setUp();
        when(topicRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicService.getTopic(99L, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("99");
    }

    @Test
    void selectNextTopic_returnsFirstDueTopicInRepositoryOrder() {
        setUp();
        Topic first = topicWithMemoryScore(0.1);
        Topic second = topicWithMemoryScore(0.3);
        when(topicRepository.findDueTopicsByCategory(eq(1L), eq("java"), any())).thenReturn(List.of(first, second));

        assertThat(topicService.selectNextTopic(1L, "java")).contains(first);
    }

    @Test
    void selectNextTopic_tiedMemoryScore_prefersLowerTitleNumberOverArbitraryOrder() {
        setUp();
        // Freshly imported, never-reviewed topics tie on memory score; the
        // repository happens to return "2." first (e.g. bulk-insert timing),
        // but "1." should still win the tie.
        Topic second = topicWithTitleAndMemoryScore("2. Overriding equals() and hashCode()", 0.0);
        Topic first = topicWithTitleAndMemoryScore("1. == for Primitives vs References", 0.0);
        when(topicRepository.findDueTopicsByCategory(eq(1L), eq("java"), any()))
            .thenReturn(List.of(second, first));

        assertThat(topicService.selectNextTopic(1L, "java")).contains(first);
    }

    @Test
    void selectNextTopic_tiedMemoryScoreNoExplicitOrder_prefersSubcategoryAlphabetically() {
        setUp();
        // No subcategory_order rows configured for this category, so the
        // markdown list falls back to alphabetical - Basics before Generics.
        // The repository happens to return Generics first, but Basics should
        // still win the tie.
        Topic generics = topicWithSubcategoryAndTitle("Generics", "1. Declaring a Generic Class");
        Topic basics = topicWithSubcategoryAndTitle("Basics", "1. The 8 Primitive Types");
        generics.updateMemory(0.0, LocalDateTime.now());
        basics.updateMemory(0.0, LocalDateTime.now());
        when(topicRepository.findDueTopicsByCategory(eq(1L), eq("java"), any()))
            .thenReturn(List.of(generics, basics));
        when(subcategoryOrderRepository.findByCategoryIgnoreCase("java")).thenReturn(List.of());

        assertThat(topicService.selectNextTopic(1L, "java")).contains(basics);
    }

    @Test
    void selectNextTopic_tiedMemoryScoreWithExplicitOrder_honorsSubcategoryOrderTable() {
        setUp();
        // An explicit subcategory_order row overrides the alphabetical
        // fallback, same as DashboardService.getMarkdownSummaries.
        Topic generics = topicWithSubcategoryAndTitle("Generics", "1. Declaring a Generic Class");
        Topic basics = topicWithSubcategoryAndTitle("Basics", "1. The 8 Primitive Types");
        generics.updateMemory(0.0, LocalDateTime.now());
        basics.updateMemory(0.0, LocalDateTime.now());
        when(topicRepository.findDueTopicsByCategory(eq(1L), eq("java"), any()))
            .thenReturn(List.of(basics, generics));
        when(subcategoryOrderRepository.findByCategoryIgnoreCase("java")).thenReturn(List.of(
            new dev.recallforge.domain.SubcategoryOrder("java", "Generics", 0),
            new dev.recallforge.domain.SubcategoryOrder("java", "Basics", 1)
        ));

        assertThat(topicService.selectNextTopic(1L, "java")).contains(generics);
    }

    @Test
    void selectNextTopic_returnsEmptyWhenNoTopicsAreDue() {
        setUp();
        when(topicRepository.findDueTopicsByCategory(eq(1L), eq("java"), any())).thenReturn(List.of());

        assertThat(topicService.selectNextTopic(1L, "java")).isEmpty();
    }

    @Test
    void selectNextTopicByMarkdownFileId_returnsFirstDueTopicScopedToThatFile() {
        setUp();
        Topic first = topicWithMemoryScore(0.1);
        Topic second = topicWithMemoryScore(0.4);
        when(topicRepository.findDueTopicsByMarkdownFileId(eq(7L), eq(1L), any()))
            .thenReturn(List.of(first, second));

        assertThat(topicService.selectNextTopicByMarkdownFileId(7L, 1L)).contains(first);
    }

    @Test
    void selectNextTopicByMarkdownFileId_multipleDueTopics_prefersLowerTitleNumberOverMemoryScore() {
        setUp();
        // Weaker memory score would win under plain memory-score ordering,
        // but "1." should still be reviewed before "2." when both are due.
        Topic weakerSecond = topicWithTitleAndMemoryScore("2. Wrapper Classes", 0.1);
        Topic strongerFirst = topicWithTitleAndMemoryScore("1. Primitive Types", 0.6);
        when(topicRepository.findDueTopicsByMarkdownFileId(eq(7L), eq(1L), any()))
            .thenReturn(List.of(weakerSecond, strongerFirst));

        assertThat(topicService.selectNextTopicByMarkdownFileId(7L, 1L)).contains(strongerFirst);
    }

    @Test
    void selectNextTopicByMarkdownFileId_noneDue_returnsEmptyWithoutFallingBackToAnyTopic() {
        setUp();
        when(topicRepository.findDueTopicsByMarkdownFileId(eq(7L), eq(1L), any())).thenReturn(List.of());

        assertThat(topicService.selectNextTopicByMarkdownFileId(7L, 1L)).isEmpty();
        verify(topicRepository, never()).findTopicsByMarkdownFileId(any(), any());
    }

    @Test
    void getTopicsByMarkdownFileId_noLeadingNumbers_fallsBackToRepositoryOrder() {
        setUp();
        Topic first = topicWithMemoryScore(0.1);
        Topic second = topicWithMemoryScore(0.4);
        when(topicRepository.findByMarkdownFileIdAndUserIdOrderByIdAsc(7L, 1L))
            .thenReturn(List.of(first, second));

        assertThat(topicService.getTopicsByMarkdownFileId(7L, 1L)).containsExactly(first, second);
    }

    @Test
    void getTopicsByMarkdownFileId_sortsByLeadingNumberInTitle() {
        setUp();
        Topic third = topicWithTitle("3. Streams");
        Topic renamedFirst = topicWithTitle("1. Primitive Types (renamed)");
        Topic second = topicWithTitle("2. Wrapper Classes");

        // Repository returns id order: the renamed topic was recreated with
        // a new (highest) id, so it comes back last even though it's "1.".
        when(topicRepository.findByMarkdownFileIdAndUserIdOrderByIdAsc(7L, 1L))
            .thenReturn(List.of(third, second, renamedFirst));

        assertThat(topicService.getTopicsByMarkdownFileId(7L, 1L))
            .containsExactly(renamedFirst, second, third);
    }

    @Test
    void countDueTopics_delegatesUserAndTimeToRepository() {
        setUp();
        LocalDateTime now = LocalDateTime.now();
        when(topicRepository.countDueByUser(1L, "java", now)).thenReturn(4L);

        assertThat(topicService.countDueTopics(1L, "java", now)).isEqualTo(4L);
    }

    @Test
    void countDueTopicsByMarkdownFileId_delegatesAllArgsToRepository() {
        setUp();
        LocalDateTime now = LocalDateTime.now();
        when(topicRepository.countDueByMarkdownFileIdAndUser(7L, 1L, now)).thenReturn(2L);

        assertThat(topicService.countDueTopicsByMarkdownFileId(7L, 1L, now)).isEqualTo(2L);
    }

    @Test
    void findNextReviewAtAfter_returnsFirstResultAndRequestsOnlyOnePage() {
        setUp();
        LocalDateTime now = LocalDateTime.now();
        Topic topic = topicWithMemoryScore(0.5);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(topicRepository.findNextByUser(eq(1L), eq("java"), eq(now), pageableCaptor.capture()))
            .thenReturn(List.of(topic));

        Optional<Topic> result = topicService.findNextReviewAtAfter(1L, "java", now);

        assertThat(result).contains(topic);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
    }

    @Test
    void findNextReviewAtAfterByMarkdownFileId_returnsFirstResultAndRequestsOnlyOnePage() {
        setUp();
        LocalDateTime now = LocalDateTime.now();
        Topic topic = topicWithMemoryScore(0.5);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(topicRepository.findNextByMarkdownFileIdAndUser(
                eq(7L), eq(1L), eq(now), pageableCaptor.capture()))
            .thenReturn(List.of(topic));

        Optional<Topic> result = topicService.findNextReviewAtAfterByMarkdownFileId(7L, 1L, now);

        assertThat(result).contains(topic);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(1);
    }

    @Test
    void save_delegatesToRepositoryAndReturnsItsResult() {
        setUp();
        Topic topic = topicWithMemoryScore(0.5);
        when(topicRepository.save(topic)).thenReturn(topic);

        assertThat(topicService.save(topic)).isSameAs(topic);
        verify(topicRepository).save(topic);
    }
}

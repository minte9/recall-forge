package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.Topic;
import dev.recallforge.dto.RecallInsightResponse;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class RecallInsightServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CurrentUserService currentUserService;

    private RecallInsightService service;

    @BeforeEach
    void setUp() {
        service = new RecallInsightService(topicRepository, currentUserService, reviewRepository);
        when(currentUserService.currentUserId()).thenReturn(1L);
    }

    @Test
    void getInsight_returnsWeakestReviewedTopicWithIdsForNavigation() {
        MarkdownFile file = mock(MarkdownFile.class);
        when(file.getId()).thenReturn(42L);
        when(file.getSubcategory()).thenReturn("Collections");

        Topic weakest = topic(7L, "3. HashMap internals", 0.31, file);
        Topic unreviewed = mock(Topic.class);
        when(unreviewed.getId()).thenReturn(8L);

        when(topicRepository.findDueTopicsByCategory(eq(1L), eq("Java"), any()))
                .thenReturn(List.of(unreviewed, weakest));
        when(reviewRepository.findReviewedTopicIds(1L, "Java")).thenReturn(Set.of(7L));

        RecallInsightResponse response = service.getInsight("Java");

        assertThat(response.weakestTopicId()).isEqualTo(7L);
        assertThat(response.weakestMarkdownFileId()).isEqualTo(42L);
        assertThat(response.weakestTopic()).isEqualTo("HashMap internals");
        assertThat(response.weakestSubcategory()).isEqualTo("Collections");
        assertThat(response.topicCount()).isEqualTo(2);
        assertThat(response.reviewedTopicCount()).isEqualTo(1);
        assertThat(response.message())
                .isEqualTo("Your weakest Java topic is HashMap internals (score: 0.31) in Collections subcategory.");
    }

    @Test
    void getInsight_withNoTopics_hasNoTopicToOpen() {
        when(topicRepository.findDueTopicsByCategory(eq(1L), eq("Java"), any()))
                .thenReturn(List.of());

        RecallInsightResponse response = service.getInsight("Java");

        assertThat(response.message()).isEqualTo("No due topics in Java.");
        assertThat(response.weakestTopicId()).isNull();
        assertThat(response.weakestMarkdownFileId()).isNull();
    }

    @Test
    void getInsight_withNoReviewedTopics_hasNoTopicToOpen() {
        Topic unreviewed = mock(Topic.class);
        when(unreviewed.getId()).thenReturn(8L);

        when(topicRepository.findDueTopicsByCategory(eq(1L), eq("Java"), any()))
                .thenReturn(List.of(unreviewed));
        when(reviewRepository.findReviewedTopicIds(1L, "Java")).thenReturn(Set.of());

        RecallInsightResponse response = service.getInsight("Java");

        assertThat(response.message()).isEqualTo("No reviewed topics are due in Java.");
        assertThat(response.topicCount()).isEqualTo(1);
        assertThat(response.reviewedTopicCount()).isZero();
        assertThat(response.weakestTopicId()).isNull();
        assertThat(response.weakestMarkdownFileId()).isNull();
    }

    private Topic topic(Long id, String title, double score, MarkdownFile file) {
        Topic topic = mock(Topic.class);
        when(topic.getId()).thenReturn(id);
        when(topic.getTitle()).thenReturn(title);
        when(topic.getMemoryScore()).thenReturn(score);
        when(topic.getMarkdownFile()).thenReturn(file);
        return topic;
    }
}

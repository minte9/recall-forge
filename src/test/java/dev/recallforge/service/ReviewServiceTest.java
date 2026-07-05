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
import org.springframework.test.util.ReflectionTestUtils;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.Review;
import dev.recallforge.domain.Topic;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.dto.AnswerResponse;
import dev.recallforge.dto.ReviewHistoryResponse;
import dev.recallforge.dto.ReviewQuestionResponse;
import dev.recallforge.dto.ReviewQueueResponse;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private TopicService topicService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OpenAiService openAiService;

    @Mock
    private RepetitionService repetitionService;

    @Mock
    private RewardService rewardService;

    @Mock
    private CurrentUserService currentUserService;

    private ReviewService reviewService;

    private void setUp() {
        reviewService = new ReviewService(
            topicService, reviewRepository, openAiService,
            repetitionService, rewardService, currentUserService
        );
    }

    private Topic topicWithId(long id, MarkdownFile markdownFile) {
        Topic topic = new Topic(
            "General", "Uncategorized", "Uncategorized", "File",
            "Title", "Content body", markdownFile, new UserAccount()
        );
        ReflectionTestUtils.setField(topic, "id", id);
        return topic;
    }

    @Test
    void startReview_noDueTopics_returnsNoReviewsDueResponse() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextTopic(1L)).thenReturn(Optional.empty());

        ReviewQuestionResponse response = reviewService.startReview(null);

        assertThat(response.done()).isTrue();
        assertThat(response.message()).isEqualTo("All reviews are done for now. Come back later.");
        assertThat(response.topicId()).isNull();
        verify(openAiService, never()).generateQuestion(any(), any());
    }

    @Test
    void startReview_topicWithoutMarkdownFile_buildsFallbackMarkdownContent() {
        setUp();
        Topic topic = topicWithId(10L, null);
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextTopic(1L)).thenReturn(Optional.of(topic));
        when(openAiService.generateQuestion("Title", "Content body")).thenReturn("What is X?");

        ReviewQuestionResponse response = reviewService.startReview(null);

        assertThat(response.topicId()).isEqualTo(10L);
        assertThat(response.question()).isEqualTo("What is X?");
        assertThat(response.markdownContent()).isEqualTo("# Title\n\nContent body");
        assertThat(response.markdownFileId()).isNull();
        assertThat(response.done()).isFalse();
    }

    @Test
    void startReview_topicWithMarkdownFile_usesMarkdownFileContentAndId() {
        setUp();
        MarkdownFile markdownFile = new MarkdownFile(
            "file.md", "# File content", "hash", "General",
            "Uncategorized", "Uncategorized", "group", new UserAccount()
        );
        ReflectionTestUtils.setField(markdownFile, "id", 99L);
        Topic topic = topicWithId(11L, markdownFile);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextTopic(1L)).thenReturn(Optional.of(topic));
        when(openAiService.generateQuestion(any(), any())).thenReturn("Q");

        ReviewQuestionResponse response = reviewService.startReview(null);

        assertThat(response.markdownContent()).isEqualTo("# File content");
        assertThat(response.markdownFileId()).isEqualTo(99L);
    }

    @Test
    void startReview_withMarkdownFileId_selectsNextTopicScopedToThatFile() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextTopicByMarkdownFileId(5L, 1L)).thenReturn(Optional.empty());

        ReviewQuestionResponse response = reviewService.startReview(5L);

        assertThat(response.done()).isTrue();
        verify(topicService).selectNextTopicByMarkdownFileId(5L, 1L);
        verify(topicService, never()).selectNextTopic(any());
    }

    @Test
    void saveAnswer_updatesTopicMemoryAwardsRewardAndReturnsResponse() {
        setUp();
        Topic topic = topicWithId(7L, null);
        EvaluationResult evaluation = new EvaluationResult(0.8, "Good job");
        LocalDateTime nextReviewAt = LocalDateTime.now().plusDays(7);
        RewardResponse reward = new RewardResponse(100, 2, "Student", 0, 100, 3, 6, "Java", 4L, 8L, 50);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(7L, 1L)).thenReturn(topic);
        when(currentUserService.currentUser()).thenReturn(new UserAccount());
        when(repetitionService.calculateUpdateMemoryScore(0.5, 0.8)).thenReturn(0.65);
        when(repetitionService.calculateNextReviewAt(0.8)).thenReturn(nextReviewAt);
        when(rewardService.rewardDefaultUser(0.8, "Uncategorized")).thenReturn(reward);
        when(reviewRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AnswerResponse response = reviewService.saveAnswer(7L, "Q?", "My answer", evaluation);

        assertThat(response.topicId()).isEqualTo(7L);
        assertThat(response.topicTitle()).isEqualTo("Title");
        assertThat(response.score()).isEqualTo(0.8);
        assertThat(response.feedback()).isEqualTo("Good job");
        assertThat(response.updateMemoryScore()).isEqualTo(0.65);
        assertThat(response.nextReviewAt()).isEqualTo(nextReviewAt);
        assertThat(response.reward()).isEqualTo(reward);

        assertThat(topic.getMemoryScore()).isEqualTo(0.65);
        assertThat(topic.getNextReviewAt()).isEqualTo(nextReviewAt);
        verify(topicService).save(topic);

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review savedReview = reviewCaptor.getValue();
        assertThat(savedReview.getQuestion()).isEqualTo("Q?");
        assertThat(savedReview.getUserAnswer()).isEqualTo("My answer");
        assertThat(savedReview.getScore()).isEqualTo(0.8);
        assertThat(savedReview.getFeedback()).isEqualTo("Good job");
    }

    @Test
    void getReviewHistoryForTopic_throwsWhenTopicNotOwnedByUser_andSkipsRepositoryQuery() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(99L, 1L))
            .thenThrow(new IllegalArgumentException("Topic not found: 99"));

        assertThatThrownBy(() -> reviewService.getReviewHistoryForTopic(99L))
            .isInstanceOf(IllegalArgumentException.class);

        verify(reviewRepository, never())
            .findByTopicIdAndTopicUserIdOrderByReviewedAtDesc(any(), any());
    }

    @Test
    void getReviewHistoryForTopic_returnsHistoryMappedFromRepository() {
        setUp();
        Topic topic = topicWithId(3L, null);
        Review review = new Review(topic, new UserAccount(), "Q1", "A1", 0.9, "fb1");
        ReflectionTestUtils.setField(review, "id", 1L);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(3L, 1L)).thenReturn(topic);
        when(reviewRepository.findByTopicIdAndTopicUserIdOrderByReviewedAtDesc(3L, 1L))
            .thenReturn(List.of(review));

        List<ReviewHistoryResponse> history = reviewService.getReviewHistoryForTopic(3L);

        assertThat(history).hasSize(1);
        ReviewHistoryResponse entry = history.get(0);
        assertThat(entry.id()).isEqualTo(1L);
        assertThat(entry.topicId()).isEqualTo(3L);
        assertThat(entry.topicTitle()).isEqualTo("Title");
        assertThat(entry.question()).isEqualTo("Q1");
        assertThat(entry.userAnswer()).isEqualTo("A1");
        assertThat(entry.score()).isEqualTo(0.9);
        assertThat(entry.feedback()).isEqualTo("fb1");
    }

    @Test
    void getDailyQueue_withoutMarkdownFileId_usesGlobalCountsAndIsDoneWhenZero() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.countDueTopics(eq(1L), any())).thenReturn(0L);
        when(topicService.findNextReviewAtAfter(eq(1L), any())).thenReturn(Optional.empty());

        ReviewQueueResponse response = reviewService.getDailyQueue(null);

        assertThat(response.dueCount()).isZero();
        assertThat(response.nextReviewAt()).isNull();
        assertThat(response.doneForToday()).isTrue();
        verify(topicService, never()).countDueTopicsByMarkdownFileId(any(), any(), any());
    }

    @Test
    void getDailyQueue_withMarkdownFileId_usesPerFileCountsAndIsNotDoneWhenPositive() {
        setUp();
        Topic nextTopic = topicWithId(20L, null);
        nextTopic.updateMemory(0.5, LocalDateTime.now().plusDays(1));

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.countDueTopicsByMarkdownFileId(eq(5L), eq(1L), any())).thenReturn(3L);
        when(topicService.findNextReviewAtAfterByMarkdownFileId(eq(5L), eq(1L), any()))
            .thenReturn(Optional.of(nextTopic));

        ReviewQueueResponse response = reviewService.getDailyQueue(5L);

        assertThat(response.dueCount()).isEqualTo(3L);
        assertThat(response.nextReviewAt()).isEqualTo(nextTopic.getNextReviewAt());
        assertThat(response.doneForToday()).isFalse();
        verify(topicService, never()).countDueTopics(any(), any());
    }
}

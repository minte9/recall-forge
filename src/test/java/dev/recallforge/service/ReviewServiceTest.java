package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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

import dev.recallforge.config.AppAiProperties;
import dev.recallforge.config.AppReviewLimitProperties;
import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.Review;
import dev.recallforge.domain.RetryType;
import dev.recallforge.domain.Topic;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.dto.AnswerResponse;
import dev.recallforge.dto.FeedbackHistoryResponse;
import dev.recallforge.dto.ReviewHistoryResponse;
import dev.recallforge.dto.ReviewQuestionResponse;
import dev.recallforge.dto.ReviewQueueResponse;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.exception.DailyQuestionLimitExceededException;
import dev.recallforge.exception.DailyReviewLimitExceededException;
import dev.recallforge.repository.QuestionGenerationRepository;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private TopicService topicService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private QuestionGenerationRepository questionGenerationRepository;

    @Mock
    private OpenAiService openAiService;

    @Mock
    private RepetitionService repetitionService;

    @Mock
    private RewardService rewardService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AppReviewLimitProperties reviewLimitProperties;

    @Mock
    private AppAiProperties aiProperties;

    private ReviewService reviewService;

    private void setUp() {
        reviewService = new ReviewService(
            topicService, reviewRepository, questionGenerationRepository, openAiService,
            repetitionService, rewardService, currentUserService,
            reviewLimitProperties, aiProperties
        );

        lenient().when(reviewLimitProperties.dailyQuestionLimit()).thenReturn(50);
        lenient().when(reviewLimitProperties.dailyUserQuestionLimit()).thenReturn(100);
        lenient().when(reviewLimitProperties.dailyUserReviewLimit()).thenReturn(100);
        lenient().when(aiProperties.enabled()).thenReturn(true);
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
        when(topicService.selectNextTopic(1L, "java")).thenReturn(Optional.empty());

        ReviewQuestionResponse response = reviewService.startReview(null, "java", null);

        assertThat(response.done()).isTrue();
        assertThat(response.message()).isEqualTo("All reviews are done for now. Come back later.");
        assertThat(response.topicId()).isNull();
        verify(openAiService, never()).generateQuestion(any(), any(), anyDouble(), any());
    }

    @Test
    void startReview_topicWithoutMarkdownFile_buildsFallbackMarkdownContent() {
        setUp();
        Topic topic = topicWithId(10L, null);
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextTopic(1L, "java")).thenReturn(Optional.of(topic));
        when(openAiService.generateQuestion(eq("Title"), eq("Content body"), eq(0.6), any()))
            .thenReturn("What is X?");

        ReviewQuestionResponse response = reviewService.startReview(null, "java", null);

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
        when(topicService.selectNextTopic(1L, "java")).thenReturn(Optional.of(topic));
        when(openAiService.generateQuestion(any(), any(), anyDouble(), any())).thenReturn("Q");

        ReviewQuestionResponse response = reviewService.startReview(null, "java", null);

        assertThat(response.markdownContent()).isEqualTo("# File content");
        assertThat(response.markdownFileId()).isEqualTo(99L);
    }

    // Regression test for the AI badge not showing when a review is started
    // directly (Start Review) instead of via previewMarkdownFile - the
    // response must carry the file's own optimized/aiGenerated/original
    // state, not just its content/id.
    @Test
    void startReview_topicWithOptimizedMarkdownFile_carriesOptimizationMetadata() {
        setUp();
        MarkdownFile markdownFile = new MarkdownFile(
            "file.md", "# File content", "hash", "General",
            "Uncategorized", "Uncategorized", "group", new UserAccount()
        );
        markdownFile.markAiGenerated();
        markdownFile.markOptimized("# Original content");
        Topic topic = topicWithId(11L, markdownFile);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextTopic(1L, "java")).thenReturn(Optional.of(topic));
        when(openAiService.generateQuestion(any(), any(), anyDouble(), any())).thenReturn("Q");

        ReviewQuestionResponse response = reviewService.startReview(null, "java", null);

        assertThat(response.optimized()).isTrue();
        assertThat(response.aiGenerated()).isTrue();
        assertThat(response.originalContent()).isEqualTo("# Original content");
    }

    @Test
    void startReview_withMarkdownFileId_selectsNextTopicScopedToThatFile() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextTopicByMarkdownFileId(5L, 1L)).thenReturn(Optional.empty());

        ReviewQuestionResponse response = reviewService.startReview(5L, "java", null);

        assertThat(response.done()).isTrue();
        verify(topicService).selectNextTopicByMarkdownFileId(5L, 1L);
        verify(topicService, never()).selectNextTopic(any(), any());
    }

    @Test
    void startReview_withExplicitTopicId_bypassesSelectionAndUsesThatTopicRegardlessOfDueStatus() {
        setUp();
        Topic topic = topicWithId(42L, null);
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(42L, 1L)).thenReturn(topic);
        when(openAiService.generateQuestion(eq("Title"), eq("Content body"), eq(0.6), any()))
            .thenReturn("What is X?");

        ReviewQuestionResponse response = reviewService.startReview(5L, "java", 42L);

        assertThat(response.topicId()).isEqualTo(42L);
        assertThat(response.question()).isEqualTo("What is X?");
        assertThat(response.done()).isFalse();
        verify(topicService, never()).selectNextTopic(any(), any());
        verify(topicService, never()).selectNextTopicByMarkdownFileId(any(), any());
    }

    @Test
    void startReview_withExplicitTopicId_notDueYet_forcesPracticeMode() {
        setUp();
        Topic topic = topicWithId(42L, null);
        ReflectionTestUtils.setField(topic, "nextReviewAt", LocalDateTime.now().plusDays(7));
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(42L, 1L)).thenReturn(topic);
        when(openAiService.generateQuestion(any(), any(), anyDouble(), any())).thenReturn("What is X?");

        ReviewQuestionResponse response = reviewService.startReview(5L, "java", 42L);

        assertThat(response.forcePractice()).isTrue();
    }

    @Test
    void startReview_withExplicitTopicId_alreadyReviewedToday_forcesPracticeMode() {
        setUp();
        Topic topic = topicWithId(42L, null);
        ReflectionTestUtils.setField(topic, "nextReviewAt", LocalDateTime.now().minusDays(1));
        Review earlierToday = new Review(
            topic, new UserAccount(), "Old Q", "A1", 0.9, "fb1", 50, false, RetryType.REVIEW);
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(42L, 1L)).thenReturn(topic);
        when(reviewRepository.findTop3ByTopicIdAndTopicUserIdOrderByReviewedAtDesc(42L, 1L))
            .thenReturn(List.of(earlierToday));
        when(openAiService.generateQuestion(any(), any(), anyDouble(), any())).thenReturn("What is X?");

        ReviewQuestionResponse response = reviewService.startReview(5L, "java", 42L);

        assertThat(response.forcePractice()).isTrue();
    }

    @Test
    void startReview_topicDueAndNotReviewedToday_doesNotForcePracticeMode() {
        setUp();
        Topic topic = topicWithId(10L, null);
        ReflectionTestUtils.setField(topic, "nextReviewAt", LocalDateTime.now().minusDays(1));
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextTopic(1L, "java")).thenReturn(Optional.of(topic));
        when(openAiService.generateQuestion(any(), any(), anyDouble(), any())).thenReturn("What is X?");

        ReviewQuestionResponse response = reviewService.startReview(null, "java", null);

        assertThat(response.forcePractice()).isFalse();
    }

    @Test
    void startReview_dailyQuestionLimitReached_throwsAndSkipsQuestionGeneration() {
        setUp();
        Topic topic = topicWithId(10L, null);
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextTopic(1L, "java")).thenReturn(Optional.of(topic));
        when(reviewLimitProperties.dailyQuestionLimit()).thenReturn(50);
        when(questionGenerationRepository.countByUserIdAndCategoryAndGeneratedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(50L);

        assertThatThrownBy(() -> reviewService.startReview(null, "java", null))
            .isInstanceOf(DailyQuestionLimitExceededException.class);

        verify(openAiService, never()).generateQuestion(any(), any(), anyDouble(), any());
        verify(questionGenerationRepository, never()).save(any());
    }

    @Test
    void startReview_userDailyQuestionLimitReached_throwsEvenWhenCategoryIsBelowItsLimit() {
        setUp();
        Topic topic = topicWithId(10L, null);
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextTopic(1L, "java")).thenReturn(Optional.of(topic));
        when(questionGenerationRepository.countByUserIdAndCategoryAndGeneratedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(5L);
        when(questionGenerationRepository.countByUserIdAndGeneratedAtAfter(eq(1L), any()))
            .thenReturn(100L);

        assertThatThrownBy(() -> reviewService.startReview(null, "java", null))
            .isInstanceOf(DailyQuestionLimitExceededException.class)
            .hasMessageContaining("across all categories");

        verify(openAiService, never()).generateQuestion(any(), any(), anyDouble(), any());
        verify(questionGenerationRepository, never()).save(any());
    }

    @Test
    void startReview_belowDailyQuestionLimit_generatesQuestionAndRecordsGeneration() {
        setUp();
        Topic topic = topicWithId(10L, null);
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextTopic(1L, "java")).thenReturn(Optional.of(topic));
        when(reviewLimitProperties.dailyQuestionLimit()).thenReturn(50);
        when(questionGenerationRepository.countByUserIdAndCategoryAndGeneratedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(49L);
        when(openAiService.generateQuestion(any(), any(), anyDouble(), any())).thenReturn("What is X?");

        ReviewQuestionResponse response = reviewService.startReview(null, "java", null);

        assertThat(response.question()).isEqualTo("What is X?");
        verify(questionGenerationRepository).save(any());
    }

    @Test
    void startCodeChallenge_belowDailyQuestionLimit_generatesChallengeAndFlagsResponse() {
        setUp();
        Topic topic = topicWithId(30L, null);
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(30L, 1L)).thenReturn(topic);
        when(reviewLimitProperties.dailyQuestionLimit()).thenReturn(50);
        when(questionGenerationRepository.countByUserIdAndCategoryAndGeneratedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(10L);
        when(openAiService.generateCodeChallenge("Uncategorized", "Title", "Content body", 0.6, List.of()))
            .thenReturn("Write a function that reverses a string.");

        ReviewQuestionResponse response = reviewService.startCodeChallenge(30L);

        assertThat(response.question()).isEqualTo("Write a function that reverses a string.");
        assertThat(response.codeChallenge()).isTrue();
        assertThat(response.forcePractice()).isTrue();
        verify(questionGenerationRepository).save(any());
    }

    @Test
    void startCodeChallenge_passesDistinctRecentChallengeQuestionsToOpenAiService() {
        setUp();
        Topic topic = topicWithId(30L, null);
        UserAccount user = new UserAccount();
        Review resubmission = new Review(
            topic, user, "Write a function that reverses a string.", "answer1", 0.5, "fb1", 80,
            true, RetryType.CODE_CHALLENGE);
        Review sameQuestionRetried = new Review(
            topic, user, "Write a function that reverses a string.", "answer2", 0.9, "fb2", 90,
            true, RetryType.CODE_CHALLENGE);
        Review olderDistinctChallenge = new Review(
            topic, user, "Write a function that sorts a list.", "answer3", 0.8, "fb3", 70,
            true, RetryType.CODE_CHALLENGE);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(30L, 1L)).thenReturn(topic);
        when(reviewLimitProperties.dailyQuestionLimit()).thenReturn(50);
        when(questionGenerationRepository.countByUserIdAndCategoryAndGeneratedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(10L);
        when(reviewRepository.findByTopicIdAndTopicUserIdAndRetryTypeOrderByReviewedAtDesc(
                30L, 1L, RetryType.CODE_CHALLENGE))
            .thenReturn(List.of(sameQuestionRetried, resubmission, olderDistinctChallenge));
        when(openAiService.generateCodeChallenge(any(), any(), any(), anyDouble(), any()))
            .thenReturn("New challenge.");

        reviewService.startCodeChallenge(30L);

        verify(openAiService).generateCodeChallenge(
            "Uncategorized", "Title", "Content body", 0.6,
            List.of("Write a function that reverses a string.", "Write a function that sorts a list."));
    }

    @Test
    void startCodeChallenge_dailyQuestionLimitReached_throwsAndSkipsGeneration() {
        setUp();
        Topic topic = topicWithId(30L, null);
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(30L, 1L)).thenReturn(topic);
        when(reviewLimitProperties.dailyQuestionLimit()).thenReturn(50);
        when(questionGenerationRepository.countByUserIdAndCategoryAndGeneratedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(50L);

        assertThatThrownBy(() -> reviewService.startCodeChallenge(30L))
            .isInstanceOf(DailyQuestionLimitExceededException.class);

        verify(openAiService, never()).generateCodeChallenge(any(), any(), any(), anyDouble(), any());
        verify(questionGenerationRepository, never()).save(any());
    }

    @Test
    void startCodeChallenge_userDailyQuestionLimitReached_throwsEvenWhenCategoryIsBelowItsLimit() {
        setUp();
        Topic topic = topicWithId(30L, null);
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(30L, 1L)).thenReturn(topic);
        when(questionGenerationRepository.countByUserIdAndCategoryAndGeneratedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(5L);
        when(questionGenerationRepository.countByUserIdAndGeneratedAtAfter(eq(1L), any()))
            .thenReturn(100L);

        assertThatThrownBy(() -> reviewService.startCodeChallenge(30L))
            .isInstanceOf(DailyQuestionLimitExceededException.class)
            .hasMessageContaining("across all categories");

        verify(openAiService, never()).generateCodeChallenge(any(), any(), any(), anyDouble(), any());
        verify(questionGenerationRepository, never()).save(any());
    }

    @Test
    void startCategoryCodeChallenge_picksTopicViaTopicServiceAndGeneratesChallenge() {
        setUp();
        Topic topic = topicWithId(30L, null);
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextCodeChallengeTopic(1L, "java")).thenReturn(Optional.of(topic));
        when(reviewLimitProperties.dailyQuestionLimit()).thenReturn(50);
        when(questionGenerationRepository.countByUserIdAndCategoryAndGeneratedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(10L);
        when(openAiService.generateCodeChallenge("Uncategorized", "Title", "Content body", 0.6, List.of()))
            .thenReturn("Write a function that reverses a string.");

        ReviewQuestionResponse response = reviewService.startCategoryCodeChallenge("java");

        assertThat(response.question()).isEqualTo("Write a function that reverses a string.");
        assertThat(response.topicId()).isEqualTo(30L);
        assertThat(response.codeChallenge()).isTrue();
        assertThat(response.forcePractice()).isTrue();
        verify(questionGenerationRepository).save(any());
    }

    @Test
    void startCategoryCodeChallenge_noTopicsInCategory_returnsNoReviewsDueAndSkipsGeneration() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextCodeChallengeTopic(1L, "java")).thenReturn(Optional.empty());

        ReviewQuestionResponse response = reviewService.startCategoryCodeChallenge("java");

        assertThat(response.done()).isTrue();
        assertThat(response.message()).isEqualTo("All reviews are done for now. Come back later.");
        verify(openAiService, never()).generateCodeChallenge(any(), any(), any(), anyDouble(), any());
        verify(questionGenerationRepository, never()).save(any());
    }

    @Test
    void startCategoryCodeChallenge_dailyQuestionLimitReached_throwsAndSkipsGeneration() {
        setUp();
        Topic topic = topicWithId(30L, null);
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextCodeChallengeTopic(1L, "java")).thenReturn(Optional.of(topic));
        when(reviewLimitProperties.dailyQuestionLimit()).thenReturn(50);
        when(questionGenerationRepository.countByUserIdAndCategoryAndGeneratedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(50L);

        assertThatThrownBy(() -> reviewService.startCategoryCodeChallenge("java"))
            .isInstanceOf(DailyQuestionLimitExceededException.class);

        verify(openAiService, never()).generateCodeChallenge(any(), any(), any(), anyDouble(), any());
        verify(questionGenerationRepository, never()).save(any());
    }

    @Test
    void skipTopic_pushesNextReviewAtOneDayWithoutChangingMemoryScoreOrWritingReview() {
        setUp();
        Topic topic = topicWithId(20L, null);
        double originalMemoryScore = topic.getMemoryScore();
        LocalDateTime skipReviewAt = LocalDateTime.now().plusDays(1);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(20L, 1L)).thenReturn(topic);
        when(topicService.selectNextTopic(1L, "java")).thenReturn(Optional.of(topic));
        when(repetitionService.calculateSkipReviewAt()).thenReturn(skipReviewAt);
        when(openAiService.generateQuestion(any(), any(), anyDouble(), any())).thenReturn("New Q");

        reviewService.skipTopic(20L, null, "java");

        assertThat(topic.getMemoryScore()).isEqualTo(originalMemoryScore);
        assertThat(topic.getNextReviewAt()).isEqualTo(skipReviewAt);
        verify(topicService).save(topic);
        verify(topicService).flush();
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void skipTopic_returnsFreshQuestionFromStartReviewFlow() {
        setUp();
        Topic topic = topicWithId(20L, null);
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(20L, 1L)).thenReturn(topic);
        when(topicService.selectNextTopic(1L, "java")).thenReturn(Optional.of(topic));
        when(openAiService.generateQuestion(any(), any(), anyDouble(), any())).thenReturn("Fresh question");

        ReviewQuestionResponse response = reviewService.skipTopic(20L, null, "java");

        assertThat(response.question()).isEqualTo("Fresh question");
        assertThat(response.done()).isFalse();
        verify(topicService).selectNextTopic(1L, "java");
    }

    @Test
    void skipTopic_topicNotOwnedByUser_throwsAndSkipsMutation() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(99L, 1L))
            .thenThrow(new IllegalArgumentException("Topic not found: 99"));

        assertThatThrownBy(() -> reviewService.skipTopic(99L, null, "java"))
            .isInstanceOf(IllegalArgumentException.class);

        verify(topicService, never()).save(any());
        verify(topicService, never()).flush();
    }

    @Test
    void startReview_passesTopicMemoryScoreAndRecentReviewSummariesToQuestionGeneration() {
        setUp();
        Topic topic = topicWithId(15L, null);
        Review review1 = new Review(
            topic, new UserAccount(), "Old Q1", "A1", 0.9, "fb1", 50, false, RetryType.REVIEW);
        Review review2 = new Review(
            topic, new UserAccount(), "Old Q2", "A2", 0.4, "fb2", 60, false, RetryType.REVIEW);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.selectNextTopic(1L, "java")).thenReturn(Optional.of(topic));
        when(reviewRepository.findTop3ByTopicIdAndTopicUserIdOrderByReviewedAtDesc(15L, 1L))
            .thenReturn(List.of(review1, review2));
        when(openAiService.generateQuestion(any(), any(), anyDouble(), any())).thenReturn("New question");

        reviewService.startReview(null, "java", null);

        ArgumentCaptor<Double> memoryScoreCaptor = ArgumentCaptor.forClass(Double.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReviewSummary>> reviewsCaptor = ArgumentCaptor.forClass(List.class);
        verify(openAiService).generateQuestion(
            eq("Title"), eq("Content body"), memoryScoreCaptor.capture(), reviewsCaptor.capture());

        assertThat(memoryScoreCaptor.getValue()).isEqualTo(0.6);
        assertThat(reviewsCaptor.getValue()).containsExactly(
            new ReviewSummary("Old Q1", 0.9, "fb1"),
            new ReviewSummary("Old Q2", 0.4, "fb2"));
    }

    @Test
    void saveAnswer_updatesTopicMemoryAwardsRewardAndReturnsResponse() {
        setUp();
        Topic topic = topicWithId(7L, null);
        EvaluationResult evaluation = new EvaluationResult(0.8, "Good job", 120);
        LocalDateTime nextReviewAt = LocalDateTime.now().plusDays(7);
        RewardResponse reward = new RewardResponse(3, 0, false, 10, 3, "Java", 0L, 0, 4L, 8L, 50, 0L, 0, 0L, 0, 0L, 0L, 0L, false);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(7L, 1L)).thenReturn(topic);
        when(currentUserService.currentUser()).thenReturn(new UserAccount());
        when(repetitionService.calculateUpdateMemoryScore(0.6, 0.8)).thenReturn(0.65);
        when(repetitionService.calculateNextReview(0.8, 0))
            .thenReturn(new RepetitionService.RepetitionResult(nextReviewAt, 1));
        when(rewardService.rewardDefaultUser(0.8, "Uncategorized", RetryType.REVIEW, false)).thenReturn(reward);
        when(reviewRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(topicService.countDueTopicsBySubcategory(eq(1L), eq("Uncategorized"), eq("Uncategorized"), any()))
            .thenReturn(5L);

        AnswerResponse response = reviewService.saveAnswer(7L, "Q?", "My answer", evaluation, false, false);

        assertThat(response.topicId()).isEqualTo(7L);
        assertThat(response.topicTitle()).isEqualTo("Title");
        assertThat(response.score()).isEqualTo(0.8);
        assertThat(response.feedback()).isEqualTo("Good job");
        assertThat(response.updateMemoryScore()).isEqualTo(0.65);
        assertThat(response.nextReviewAt()).isEqualTo(nextReviewAt);
        assertThat(response.reviewStreak()).isEqualTo(1);
        assertThat(response.reward()).isEqualTo(reward);
        assertThat(response.dueInSubcategory()).isEqualTo(5L);
        assertThat(response.subcategory()).isEqualTo("Uncategorized");

        assertThat(topic.getMemoryScore()).isEqualTo(0.65);
        assertThat(topic.getNextReviewAt()).isEqualTo(nextReviewAt);
        assertThat(topic.getReviewStreak()).isEqualTo(1);
        verify(topicService).save(topic);

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review savedReview = reviewCaptor.getValue();
        assertThat(savedReview.getQuestion()).isEqualTo("Q?");
        assertThat(savedReview.getUserAnswer()).isEqualTo("My answer");
        assertThat(savedReview.getScore()).isEqualTo(0.8);
        assertThat(savedReview.getFeedback()).isEqualTo("Good job");
        assertThat(savedReview.getTotalTokens()).isEqualTo(120);
    }

    @Test
    void saveAnswer_isRetry_savesReviewButLeavesMemoryScheduleAndStreakUnchanged() {
        setUp();
        Topic topic = topicWithId(7L, null);
        EvaluationResult evaluation = new EvaluationResult(0.9, "Good job", 120);
        LocalDateTime originalNextReviewAt = topic.getNextReviewAt();
        RewardResponse reward = new RewardResponse(3, 0, false, 10, 3, "Java", 0L, 0, 4L, 8L, 50, 0L, 0, 0L, 0, 0L, 0L, 0L, false);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(7L, 1L)).thenReturn(topic);
        when(currentUserService.currentUser()).thenReturn(new UserAccount());
        when(rewardService.rewardDefaultUser(0.9, "Uncategorized", RetryType.REVIEW, true)).thenReturn(reward);
        when(reviewRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AnswerResponse response = reviewService.saveAnswer(7L, "Q?", "My answer", evaluation, true, false);

        assertThat(response.updateMemoryScore()).isEqualTo(0.6);
        assertThat(response.nextReviewAt()).isEqualTo(originalNextReviewAt);
        assertThat(response.reviewStreak()).isZero();

        assertThat(topic.getMemoryScore()).isEqualTo(0.6);
        assertThat(topic.getNextReviewAt()).isEqualTo(originalNextReviewAt);
        assertThat(topic.getReviewStreak()).isZero();

        verify(reviewRepository).save(any());
        verify(topicService, never()).save(any());
        verify(repetitionService, never()).calculateUpdateMemoryScore(anyDouble(), anyDouble());
        verify(repetitionService, never()).calculateNextReview(anyDouble(), anyInt());

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        assertThat(reviewCaptor.getValue().getRetryType()).isEqualTo(RetryType.REVIEW);
    }

    @Test
    void answerQuestion_categoryDailyLimitReached_throwsAndSkipsEvaluation() {
        setUp();
        Topic topic = topicWithId(7L, null);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(7L, 1L)).thenReturn(topic);
        when(reviewLimitProperties.dailyLimit()).thenReturn(20);
        when(reviewRepository.countByUserIdAndCategoryAndReviewedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(20L);

        assertThatThrownBy(() -> reviewService.answerQuestion(7L, "Q?", "My answer", false, false))
            .isInstanceOf(DailyReviewLimitExceededException.class);

        verify(openAiService, never()).evaluateAnswer(any(), any(), any(), any());
    }

    @Test
    void answerQuestion_userDailyReviewLimitReached_throwsEvenWhenCategoryIsBelowItsLimit() {
        setUp();
        Topic topic = topicWithId(7L, null);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(7L, 1L)).thenReturn(topic);
        when(reviewLimitProperties.dailyLimit()).thenReturn(20);
        when(reviewRepository.countByUserIdAndCategoryAndReviewedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(2L);
        when(reviewRepository.countByUserIdAndReviewedAtAfter(eq(1L), any()))
            .thenReturn(100L);

        assertThatThrownBy(() -> reviewService.answerQuestion(7L, "Q?", "My answer", false, false))
            .isInstanceOf(DailyReviewLimitExceededException.class)
            .hasMessageContaining("across all categories");

        verify(openAiService, never()).evaluateAnswer(any(), any(), any(), any());
    }

    @Test
    void answerQuestion_belowCategoryDailyLimit_evaluatesAndSavesUsingTopicsOwnCategoryCount() {
        setUp();
        Topic topic = topicWithId(7L, null);
        EvaluationResult evaluation = new EvaluationResult(0.8, "Good job", 120);
        RewardResponse reward = new RewardResponse(3, 0, false, 10, 3, "Uncategorized", 0L, 0, 4L, 8L, 50, 0L, 0, 0L, 0, 0L, 0L, 0L, false);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(7L, 1L)).thenReturn(topic);
        when(reviewLimitProperties.dailyLimit()).thenReturn(20);
        when(reviewRepository.countByUserIdAndCategoryAndReviewedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(2L);
        when(openAiService.evaluateAnswer("Title", "Content body", "Q?", "My answer")).thenReturn(evaluation);
        when(currentUserService.currentUser()).thenReturn(new UserAccount());
        when(repetitionService.calculateUpdateMemoryScore(0.6, 0.8)).thenReturn(0.65);
        when(repetitionService.calculateNextReview(0.8, 0))
            .thenReturn(new RepetitionService.RepetitionResult(LocalDateTime.now().plusDays(7), 1));
        when(rewardService.rewardDefaultUser(0.8, "Uncategorized", RetryType.REVIEW, false)).thenReturn(reward);
        when(reviewRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AnswerResponse response = reviewService.answerQuestion(7L, "Q?", "My answer", false, false);

        assertThat(response.score()).isEqualTo(0.8);
        verify(reviewRepository).countByUserIdAndCategoryAndReviewedAtAfter(eq(1L), eq("Uncategorized"), any());

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        assertThat(reviewCaptor.getValue().isRetry()).isFalse();
    }

    @Test
    void answerQuestion_codeChallenge_evaluatesWithStrictGraderAndForcesRetry() {
        setUp();
        Topic topic = topicWithId(7L, null);
        EvaluationResult evaluation = new EvaluationResult(1.0, "Compiles and runs correctly", 120);
        RewardResponse reward = new RewardResponse(3, 0, false, 10, 3, "Uncategorized", 0L, 0, 4L, 8L, 50, 0L, 0, 0L, 0, 0L, 0L, 0L, false);
        LocalDateTime originalNextReviewAt = topic.getNextReviewAt();

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(7L, 1L)).thenReturn(topic);
        when(reviewLimitProperties.dailyLimit()).thenReturn(20);
        when(reviewRepository.countByUserIdAndCategoryAndReviewedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(2L);
        when(openAiService.evaluateCodeChallenge("Uncategorized", "Title", "Content body", "Q?", "My code"))
            .thenReturn(evaluation);
        when(currentUserService.currentUser()).thenReturn(new UserAccount());
        when(rewardService.rewardDefaultUser(1.0, "Uncategorized", RetryType.CODE_CHALLENGE, true)).thenReturn(reward);
        when(reviewRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AnswerResponse response = reviewService.answerQuestion(7L, "Q?", "My code", true, true);

        assertThat(response.score()).isEqualTo(1.0);
        assertThat(topic.getNextReviewAt()).isEqualTo(originalNextReviewAt);
        verify(openAiService, never()).evaluateAnswer(any(), any(), any(), any());
        verify(topicService, never()).save(any());

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        assertThat(reviewCaptor.getValue().isRetry()).isTrue();
        assertThat(reviewCaptor.getValue().getRetryType()).isEqualTo(RetryType.CODE_CHALLENGE);
    }

    // A fresh, first-attempt code challenge (isRetry = false) is still
    // RetryType.CODE_CHALLENGE and still never moves the topic's
    // spaced-repetition schedule - only isRetry gates memoryScore/
    // nextReviewAt/reviewStreak for plain reviews; a code challenge is
    // gated on codeChallenge regardless of isRetry, so it stays eligible
    // for goal days / XP (via rewardDefaultUser's isRetry = false) without
    // ever touching the review schedule.
    @Test
    void answerQuestion_freshCodeChallenge_isNotMarkedRetry_andStillLeavesScheduleUnchanged() {
        setUp();
        Topic topic = topicWithId(7L, null);
        EvaluationResult evaluation = new EvaluationResult(1.0, "Compiles and runs correctly", 120);
        RewardResponse reward = new RewardResponse(3, 0, false, 10, 3, "Uncategorized", 0L, 0, 4L, 8L, 50, 0L, 0, 0L, 0, 0L, 0L, 0L, false);
        LocalDateTime originalNextReviewAt = topic.getNextReviewAt();

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(7L, 1L)).thenReturn(topic);
        when(reviewLimitProperties.dailyLimit()).thenReturn(20);
        when(reviewRepository.countByUserIdAndCategoryAndReviewedAtAfter(eq(1L), eq("Uncategorized"), any()))
            .thenReturn(2L);
        when(openAiService.evaluateCodeChallenge("Uncategorized", "Title", "Content body", "Q?", "My code"))
            .thenReturn(evaluation);
        when(currentUserService.currentUser()).thenReturn(new UserAccount());
        when(rewardService.rewardDefaultUser(1.0, "Uncategorized", RetryType.CODE_CHALLENGE, false)).thenReturn(reward);
        when(reviewRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AnswerResponse response = reviewService.answerQuestion(7L, "Q?", "My code", false, true);

        assertThat(response.score()).isEqualTo(1.0);
        assertThat(topic.getNextReviewAt()).isEqualTo(originalNextReviewAt);
        verify(topicService, never()).save(any());

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        assertThat(reviewCaptor.getValue().isRetry()).isFalse();
        assertThat(reviewCaptor.getValue().getRetryType()).isEqualTo(RetryType.CODE_CHALLENGE);
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
        Review review = new Review(
            topic, new UserAccount(), "Q1", "A1", 0.9, "fb1", 100, false, RetryType.REVIEW);
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
    void getFeedbackHistory_mapsIsRetryFromReview() {
        setUp();
        Topic topic = topicWithId(3L, null);
        Review gradedReview = new Review(
            topic, new UserAccount(), "Q1", "A1", 0.9, "fb1", 100, false, RetryType.REVIEW);
        Review practiceReview = new Review(
            topic, new UserAccount(), "Q2", "A2", 0.5, "fb2", 80, true, RetryType.CODE_CHALLENGE);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.getTopic(3L, 1L)).thenReturn(topic);
        when(reviewRepository.findTop10ByTopicIdAndTopicUserIdOrderByReviewedAtDesc(3L, 1L))
            .thenReturn(List.of(practiceReview, gradedReview));

        List<FeedbackHistoryResponse> history = reviewService.getFeedbackHistory(3L);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).isRetry()).isTrue();
        assertThat(history.get(0).retryType()).isEqualTo(RetryType.CODE_CHALLENGE);
        assertThat(history.get(1).isRetry()).isFalse();
        assertThat(history.get(1).retryType()).isEqualTo(RetryType.REVIEW);
    }

    @Test
    void getDailyQueue_withoutMarkdownFileId_usesGlobalCountsAndIsDoneWhenZero() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.countDueTopics(eq(1L), eq("java"), any())).thenReturn(0L);
        when(topicService.findNextReviewAtAfter(eq(1L), eq("java"), any())).thenReturn(Optional.empty());

        ReviewQueueResponse response = reviewService.getDailyQueue(null, "java");

        assertThat(response.dueCount()).isZero();
        assertThat(response.nextReviewAt()).isNull();
        assertThat(response.doneForToday()).isTrue();
        verify(topicService, never()).countDueTopicsByMarkdownFileId(any(), any(), any());
    }

    @Test
    void getDailyQueue_withMarkdownFileId_usesPerFileCountsAndIsNotDoneWhenPositive() {
        setUp();
        Topic nextTopic = topicWithId(20L, null);
        nextTopic.updateMemory(0.5, LocalDateTime.now().plusDays(1), 0);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(topicService.countDueTopicsByMarkdownFileId(eq(5L), eq(1L), any())).thenReturn(3L);
        when(topicService.findNextReviewAtAfterByMarkdownFileId(eq(5L), eq(1L), any()))
            .thenReturn(Optional.of(nextTopic));

        ReviewQueueResponse response = reviewService.getDailyQueue(5L, "java");

        assertThat(response.dueCount()).isEqualTo(3L);
        assertThat(response.nextReviewAt()).isEqualTo(nextTopic.getNextReviewAt());
        assertThat(response.doneForToday()).isFalse();
        verify(topicService, never()).countDueTopics(any(), any(), any());
    }
}

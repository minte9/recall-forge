package dev.recallforge.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dev.recallforge.config.AppAiProperties;
import dev.recallforge.config.AppReviewLimitProperties;
import dev.recallforge.dto.FeedbackHistoryResponse;
import dev.recallforge.domain.QuestionGeneration;
import dev.recallforge.domain.Review;
import dev.recallforge.domain.Topic;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.dto.AnswerResponse;
import dev.recallforge.dto.ReviewHistoryResponse;
import dev.recallforge.dto.ReviewQuestionResponse;
import dev.recallforge.dto.ReviewQueueResponse;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.exception.AiDisabledException;
import dev.recallforge.exception.DailyQuestionLimitExceededException;
import dev.recallforge.exception.DailyReviewLimitExceededException;
import dev.recallforge.repository.QuestionGenerationRepository;
import dev.recallforge.repository.ReviewRepository;
import jakarta.transaction.Transactional;

import dev.recallforge.security.CurrentUserService;

/**
 * Orchestrates the review lifecycle for the current user: picks the next
 * due topic (via {@link TopicService}), gets a question from
 * {@link OpenAiService}, grades the submitted answer, updates the topic's
 * memory score and next-review date via {@link RepetitionService}, awards
 * XP via {@link RewardService}, and enforces the daily review/question
 * limits from {@link AppReviewLimitProperties}. {@code evaluateAnswer} and
 * {@code saveAnswer} are split out (rather than inlined in
 * {@code answerQuestion}) so a caller can grade an answer without
 * necessarily persisting it.
 */
@Service
public class ReviewService {

    private final TopicService topicService;
    private final ReviewRepository reviewRepository;
    private final QuestionGenerationRepository questionGenerationRepository;
    private final OpenAiService openAiService;
    private final RepetitionService repetitionService;
    private final RewardService rewardService;
    private final CurrentUserService currentUserService;
    private final AppReviewLimitProperties reviewLimitProperties;
    private final AppAiProperties aiProperties;

    public ReviewService(
            TopicService topicService, ReviewRepository reviewRepository,
            QuestionGenerationRepository questionGenerationRepository,
            OpenAiService openAiService, RepetitionService repetitionService,
            RewardService rewardService, CurrentUserService currentUserService,
            AppReviewLimitProperties reviewLimitProperties, AppAiProperties aiProperties) {

        this.topicService = topicService;
        this.reviewRepository = reviewRepository;
        this.questionGenerationRepository = questionGenerationRepository;
        this.openAiService = openAiService;
        this.repetitionService = repetitionService;
        this.rewardService = rewardService;
        this.currentUserService = currentUserService;
        this.reviewLimitProperties = reviewLimitProperties;
        this.aiProperties = aiProperties;
    }

    public ReviewQuestionResponse startReview(Long markdownFileId, String category, Long topicId) {
        if (!aiProperties.enabled()) {
            throw new AiDisabledException();
        }

        Long userId = currentUserService.currentUserId();

        Optional<Topic> topicOptional = topicId != null
                ? Optional.of(topicService.getTopic(topicId, userId))
                : markdownFileId == null
                    ? topicService.selectNextTopic(userId, category)
                    : topicService.selectNextTopicByMarkdownFileId(markdownFileId, userId);

        if (topicOptional.isEmpty()) {
            return ReviewQuestionResponse.noReviewsDue();
        }

        Topic topic = topicOptional.get();

        long questionsToday = questionGenerationRepository.countByUserIdAndCategoryAndGeneratedAtAfter(
                userId, topic.getCategory(), LocalDate.now().atStartOfDay());

        if (questionsToday >= reviewLimitProperties.dailyQuestionLimit()) {
            throw new DailyQuestionLimitExceededException();
        }

        List<ReviewSummary> recentReviews = reviewRepository
                .findTop3ByTopicIdAndTopicUserIdOrderByReviewedAtDesc(topic.getId(), userId)
                .stream()
                .map(r -> new ReviewSummary(r.getQuestion(), r.getScore(), r.getFeedback()))
                .toList();

        String question = openAiService.generateQuestion(
                topic.getTitle(), topic.getContent(), topic.getMemoryScore(), recentReviews);

        questionGenerationRepository.save(new QuestionGeneration(userId, topic.getCategory()));

        String markdownContent = topic.getMarkdownFile() != null
                ? topic.getMarkdownFile().getContent()
                : "# " + topic.getTitle() + "\n\n" + topic.getContent();

        Long responseMarkdownFileId = topic.getMarkdownFile() != null
                ? topic.getMarkdownFile().getId()
                : null;

        return new ReviewQuestionResponse(
                topic.getId(),
                topic.getTitle(),
                question,
                markdownContent,
                responseMarkdownFileId,
                false,
                null);
    }

    public ReviewQuestionResponse skipTopic(Long topicId, Long markdownFileId, String category) {
        Long userId = currentUserService.currentUserId();
        Topic topic = topicService.getTopic(topicId, userId);

        topic.skipReview(repetitionService.calculateSkipReviewAt());
        topicService.save(topic);
        topicService.flush();

        return startReview(markdownFileId, category, null);
    }

    public AnswerResponse answerQuestion(Long topicId, String question, String userAnswer, boolean isRetry) {
        if (!aiProperties.enabled()) {
            throw new AiDisabledException();
        }

        Long userId = currentUserService.currentUserId();
        Topic topic = topicService.getTopic(topicId, userId);

        long reviewsToday = reviewRepository.countByUserIdAndCategoryAndReviewedAtAfter(
                userId, topic.getCategory(), LocalDate.now().atStartOfDay());

        if (reviewsToday >= reviewLimitProperties.dailyLimit()) {
            throw new DailyReviewLimitExceededException();
        }

        EvaluationResult evaluation = evaluateAnswer(topicId, question, userAnswer);
        return saveAnswer(topicId, question, userAnswer, evaluation, isRetry);
    }

    public EvaluationResult evaluateAnswer(
            Long topicId,
            String question,
            String userAnswer) {
        Long userId = currentUserService.currentUserId();
        Topic topic = topicService.getTopic(topicId, userId);

        return openAiService.evaluateAnswer(
                topic.getTitle(),
                topic.getContent(),
                question,
                userAnswer);
    }

    @Transactional
    public AnswerResponse saveAnswer(
            Long topicId,
            String question,
            String userAnswer,
            EvaluationResult evaluation,
            boolean isRetry) {
        Long userId = currentUserService.currentUserId();
        Topic topic = topicService.getTopic(topicId, userId);

        UserAccount user = currentUserService.currentUser();

        Review review = new Review(
                topic,
                user,
                question,
                userAnswer,
                evaluation.score(),
                evaluation.feedback(),
                evaluation.totalTokens());

        reviewRepository.save(review);

        double updatedMemoryScore = topic.getMemoryScore();
        LocalDateTime nextReviewAt = topic.getNextReviewAt();
        int reviewStreak = topic.getReviewStreak();

        // A retry is a same-sitting redo of the currently displayed question,
        // not an independent, time-separated recall - grade and save it, but
        // don't let it move memoryScore/nextReviewAt/reviewStreak, or repeated
        // retries could inflate the streak or reset it on a single fluky redo.
        if (!isRetry) {
            updatedMemoryScore = repetitionService.calculateUpdateMemoryScore(
                    topic.getMemoryScore(),
                    evaluation.score());

            RepetitionService.RepetitionResult repetition = repetitionService.calculateNextReview(
                    evaluation.score(),
                    topic.getReviewStreak());

            nextReviewAt = repetition.nextReviewAt();
            reviewStreak = repetition.reviewStreak();

            topic.updateMemory(updatedMemoryScore, nextReviewAt, reviewStreak);
            topicService.save(topic);
            topicService.flush();
        }

        RewardResponse reward = rewardService.rewardDefaultUser(evaluation.score(), topic.getCategory());

        return new AnswerResponse(
                topic.getId(),
                topic.getTitle(),
                evaluation.score(),
                evaluation.feedback(),
                updatedMemoryScore,
                nextReviewAt,
                reviewStreak,
                reward);
    }

    public List<ReviewHistoryResponse> getReviewHistoryForTopic(Long topicId) {
        Long userId = currentUserService.currentUserId();
        topicService.getTopic(topicId, userId);

        return reviewRepository.findByTopicIdAndTopicUserIdOrderByReviewedAtDesc(topicId, userId)
                .stream()
                .map(ReviewHistoryResponse::from)
                .toList();
    }

    public ReviewQueueResponse getDailyQueue(Long markdownFileId, String category) {
        Long userId = currentUserService.currentUserId();
        LocalDateTime now = LocalDateTime.now();

        long dueCount = markdownFileId == null
                ? topicService.countDueTopics(userId, category, now)
                : topicService.countDueTopicsByMarkdownFileId(markdownFileId, userId, now);

        LocalDateTime nextReviewAt = markdownFileId == null
                ? topicService.findNextReviewAtAfter(userId, category, now)
                        .map(Topic::getNextReviewAt)
                        .orElse(null)
                : topicService.findNextReviewAtAfterByMarkdownFileId(markdownFileId, userId, now)
                        .map(Topic::getNextReviewAt)
                        .orElse(null);

        return new ReviewQueueResponse(
                dueCount,
                nextReviewAt,
                dueCount == 0);
    }

    public List<FeedbackHistoryResponse> getFeedbackHistory(Long topicId) {
        Long userId = currentUserService.currentUserId();

        topicService.getTopic(topicId, userId);

        return reviewRepository
                .findTop10ByTopicIdAndTopicUserIdOrderByReviewedAtDesc(topicId, userId)
                .stream()
                .map(r -> new FeedbackHistoryResponse(
                        r.getId(),
                        r.getQuestion(),
                        r.getUserAnswer(),
                        r.getFeedback(),
                        r.getReviewedAt(),
                        r.getScore()))
                .toList();
    }
}

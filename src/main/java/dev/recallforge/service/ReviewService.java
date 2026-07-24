package dev.recallforge.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

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
import dev.recallforge.exception.DailyQuestionLimitExceededException;
import dev.recallforge.exception.DailyReviewLimitExceededException;
import dev.recallforge.repository.QuestionGenerationRepository;
import dev.recallforge.repository.ReviewRepository;
import jakarta.transaction.Transactional;

import dev.recallforge.security.CurrentUserService;

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

    public ReviewService(
            TopicService topicService, ReviewRepository reviewRepository,
            QuestionGenerationRepository questionGenerationRepository,
            OpenAiService openAiService, RepetitionService repetitionService,
            RewardService rewardService, CurrentUserService currentUserService,
            AppReviewLimitProperties reviewLimitProperties) {

        this.topicService = topicService;
        this.reviewRepository = reviewRepository;
        this.questionGenerationRepository = questionGenerationRepository;
        this.openAiService = openAiService;
        this.repetitionService = repetitionService;
        this.rewardService = rewardService;
        this.currentUserService = currentUserService;
        this.reviewLimitProperties = reviewLimitProperties;
    }

    public ReviewQuestionResponse startReview(Long markdownFileId, String category, Long topicId) {
        Long userId = currentUserService.currentUserId();

        Optional<Topic> topicOptional = topicId != null
                ? Optional.of(topicService.getTopic(topicId, userId))
                : markdownFileId == null
                    ? topicService.selectNextTopic(userId, category)
                    : topicService.selectNextTopicByMarkdownFileId(markdownFileId, userId);

        if (topicOptional.isEmpty()) {
            return ReviewQuestionResponse.noReviewsDue();
        }

        long questionsToday = questionGenerationRepository.countByUserIdAndGeneratedAtAfter(
                userId, LocalDate.now().atStartOfDay());

        if (questionsToday >= reviewLimitProperties.dailyQuestionLimit()) {
            throw new DailyQuestionLimitExceededException();
        }

        Topic topic = topicOptional.get();

        List<ReviewSummary> recentReviews = reviewRepository
                .findTop3ByTopicIdAndTopicUserIdOrderByReviewedAtDesc(topic.getId(), userId)
                .stream()
                .map(r -> new ReviewSummary(r.getQuestion(), r.getScore(), r.getFeedback()))
                .toList();

        String question = openAiService.generateQuestion(
                topic.getTitle(), topic.getContent(), topic.getMemoryScore(), recentReviews);

        questionGenerationRepository.save(new QuestionGeneration(userId));

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

    public AnswerResponse answerQuestion(Long topicId, String question, String userAnswer) {
        Long userId = currentUserService.currentUserId();
        Topic topic = topicService.getTopic(topicId, userId);

        long reviewsToday = reviewRepository.countByUserIdAndCategoryAndReviewedAtAfter(
                userId, topic.getCategory(), LocalDate.now().atStartOfDay());

        if (reviewsToday >= reviewLimitProperties.dailyLimit()) {
            throw new DailyReviewLimitExceededException();
        }

        EvaluationResult evaluation = evaluateAnswer(topicId, question, userAnswer);
        return saveAnswer(topicId, question, userAnswer, evaluation);
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
            EvaluationResult evaluation) {
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

        double updatedMemoryScore = repetitionService.calculateUpdateMemoryScore(
                topic.getMemoryScore(),
                evaluation.score());

        LocalDateTime nextReviewAt = repetitionService.calculateNextReviewAt(evaluation.score());

        topic.updateMemory(updatedMemoryScore, nextReviewAt);
        topicService.save(topic);
        topicService.flush();

        RewardResponse reward = rewardService.rewardDefaultUser(evaluation.score(), topic.getCategory());

        return new AnswerResponse(
                topic.getId(),
                topic.getTitle(),
                evaluation.score(),
                evaluation.feedback(),
                updatedMemoryScore,
                nextReviewAt,
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

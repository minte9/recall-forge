package dev.recallforge.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dev.recallforge.domain.Review;
import dev.recallforge.domain.Topic;
import dev.recallforge.dto.AnswerResponse;
import dev.recallforge.dto.ReviewHistoryResponse;
import dev.recallforge.dto.ReviewQuestionResponse;
import dev.recallforge.dto.ReviewQueueResponse;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.repository.ReviewRepository;
import jakarta.transaction.Transactional;

@Service
public class ReviewService {
    
    private final TopicService topicService;
    private final ReviewRepository reviewRepository;
    private final OpenAiService openAiService;
    private final RepetitionService repetitionService;
    private final RewardService rewardService;

    public ReviewService(
            TopicService topicService, ReviewRepository reviewRepository, 
            OpenAiService openAiService, RepetitionService repetitionService,
            RewardService rewardService
        ) {

        this.topicService = topicService;
        this.reviewRepository = reviewRepository;
        this.openAiService = openAiService;
        this.repetitionService = repetitionService;
        this.rewardService = rewardService;
    }

    public ReviewQuestionResponse startReview(Long markdownFileId) {
        Optional<Topic> topicOptional = markdownFileId == null
                ? topicService.selectNextTopic()
                : topicService.selectNextTopicByMarkdownFileId(markdownFileId);
        
        if (topicOptional.isEmpty()) {
            return ReviewQuestionResponse.noReviewsDue();
        }

        Topic topic = topicOptional.get();

        String question = openAiService.generateQuestion(topic.getTitle(), topic.getContent());

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
                null
        );
    }

    public AnswerResponse answerQuestion(Long topicId, String question, String userAnswer) {
        EvaluationResult evaluation = evaluateAnswer(topicId, question, userAnswer);
        return saveAnswer(topicId, question, userAnswer, evaluation);
    }

    public EvaluationResult evaluateAnswer(Long topicId, String question, String userAnswer) {
        Topic topic = topicService.getTopic(topicId);
        return openAiService.evaluateAnswer(
            topic.getTitle(), 
            topic.getContent(), 
            question, 
            userAnswer
        );
    }

    @Transactional
    public AnswerResponse saveAnswer(
        Long topicId, 
        String question, 
        String userAnswer, 
        EvaluationResult evaluation
    ) {
        Topic topic = topicService.getTopic(topicId);

        Review review = new Review(topic, question, userAnswer, evaluation.score(), evaluation.feedback());
        reviewRepository.save(review);

        double updatedMemoryScore = repetitionService.calculateUpdateMemoryScore(
            topic.getMemoryScore(), 
            evaluation.score()
        );

        LocalDateTime nextReviewAt = repetitionService.calculateNextReviewAt(evaluation.score());

        topic.updateMemory(updatedMemoryScore, nextReviewAt);
        topicService.save(topic);

        RewardResponse reward = rewardService.rewardDefaultUser(evaluation.score());

        return new AnswerResponse(
            topic.getId(), 
            topic.getTitle(),
            evaluation.score(),
            evaluation.feedback(),
            updatedMemoryScore,
            nextReviewAt,
            reward
        );
    }

    public List<ReviewHistoryResponse> getReviewHistoryForTopic(Long topicId) {
        topicService.getTopic(topicId);

        return reviewRepository.findByTopicIdOrderByReviewedAtDesc(topicId)
                .stream()
                .map(ReviewHistoryResponse::from)
                .toList();
    }

    public ReviewQueueResponse getDailyQueue(Long markdownFileId) {
        LocalDateTime now = LocalDateTime.now();

        long dueCount = markdownFileId == null
            ? topicService.countDueTopics(now)
            : topicService.countDueTopicsByMarkdownFileId(markdownFileId, now);

        LocalDateTime nextReviewAt = markdownFileId == null
            ? topicService.findNextReviewAtAfter(now)
                    .map(Topic::getNextReviewAt)
                    .orElse(null)
            : topicService.findNextReviewAtAfterByMarkdownFileId(markdownFileId, now)
                    .map(Topic::getNextReviewAt)
                    .orElse(null);

        return new ReviewQueueResponse(
            dueCount,
            nextReviewAt,
            dueCount == 0
        );
    }
}

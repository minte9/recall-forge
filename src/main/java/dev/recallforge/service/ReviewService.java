package dev.recallforge.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import dev.recallforge.domain.Review;
import dev.recallforge.domain.Topic;
import dev.recallforge.dto.AnswerResponse;
import dev.recallforge.dto.ReviewQuestionResponse;
import dev.recallforge.repository.ReviewRepository;
import jakarta.transaction.Transactional;

@Service
public class ReviewService {
    
    private final TopicService topicService;
    private final ReviewRepository reviewRepository;
    private final OpenAiService openAiService;
    private final RepetitionService repetitionService;

    public ReviewService(
            TopicService topicService, ReviewRepository reviewRepository, 
            OpenAiService openAiService, RepetitionService repetitionService) {

        this.topicService = topicService;
        this.reviewRepository = reviewRepository;
        this. openAiService = openAiService;
        this.repetitionService = repetitionService;
    }

    public ReviewQuestionResponse startReview() {
        Topic topic = topicService.selectNextTopic();

        String question = openAiService.generateQuestion(
                topic.getTitle(), 
                topic.getContent()
        );

        return new ReviewQuestionResponse(
            topic.getId(),
            topic.getTitle(),
            question
        );
    }

    @Transactional
    public AnswerResponse answerQuestion(Long topicId, String question, String userAnswer) {
        Topic topic = topicService.getTopic(topicId);

        EvaluationResult evaluation = openAiService.evaluateAnswer(
            topic.getTitle(),
            topic.getContent(),
            question,
            userAnswer
        );

        Review review = new Review(
            topic,
            question,
            userAnswer,
            evaluation.score(),
            evaluation.feedback()
        );

        reviewRepository.save(review);

        double updateMemoryScore = repetitionService
            .calculateUpdateMemoryScore(
                topic.getMemoryScore(),
                evaluation.score()
            );

        LocalDateTime nextReviewAt = repetitionService.calculateNextReviewAt(
            evaluation.score()
        );

        topic.updateMemory(updateMemoryScore, nextReviewAt);
        topicService.save(topic);

        return new AnswerResponse(
            topic.getId(),
            topic.getTitle(),
            evaluation.score(),
            evaluation.feedback(),
            updateMemoryScore,
            nextReviewAt
        );
    }
}

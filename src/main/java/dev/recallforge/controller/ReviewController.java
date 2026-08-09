package dev.recallforge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import dev.recallforge.dto.AnswerRequest;
import dev.recallforge.dto.AnswerResponse;
import dev.recallforge.dto.ReviewQuestionResponse;
import dev.recallforge.dto.ReviewQueueResponse;
import dev.recallforge.dto.FeedbackHistoryResponse;
import dev.recallforge.service.ReviewService;
import jakarta.validation.Valid;

import java.util.List;


@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Starts a review session.
     *
     * The backend selects the weakest / due topic
     * and asks OpenAI to generate one question, unless a specific
     * topicId is given, in which case that exact topic is used
     * regardless of its due status.
     */
    @PostMapping("/start")
    public ReviewQuestionResponse startReview(
            @RequestParam(required = false) Long markdownFileId,
            @RequestParam(required = false, defaultValue = "Java") String category,
            @RequestParam(required = false) Long topicId) {
        return reviewService.startReview(markdownFileId, category, topicId);
    }

    /**
     * Skips the given topic for today without answering it.
     *
     * Pushes the topic's next review out by a day (no memoryScore change,
     * no Review row written) and hands back a fresh question, same shape
     * as startReview.
     */
    @PostMapping("/{topicId}/skip")
    public ReviewQuestionResponse skipTopic(
            @PathVariable Long topicId,
            @RequestParam(required = false) Long markdownFileId,
            @RequestParam(required = false, defaultValue = "Java") String category) {
        return reviewService.skipTopic(topicId, markdownFileId, category);
    }

    /**
     * Evaluates the user's answer.
     * 
     * The backend sends the question + user answer + topic content to openAI, 
     * receives a score, stores the review, and updates spaced repetition.
     */
    @PostMapping("/answer")
    public AnswerResponse answerQuestion(@Valid @RequestBody AnswerRequest request) {
        return reviewService.answerQuestion(
            request.topicId(),
            request.question(),
            request.userAnswer(),
            request.isRetry(),
            request.codeSnippet()
        );
    }

    /**
     * Starts a practice-only "Code Challenge" for the given topic: same
     * topic as the review just answered, but a code-based question that
     * always grades as a retry (see {@link ReviewService#startCodeChallenge}).
     */
    @PostMapping("/{topicId}/code-challenge")
    public ReviewQuestionResponse startCodeChallenge(@PathVariable Long topicId) {
        return reviewService.startCodeChallenge(topicId);
    }

    @GetMapping("/queue/today")
    public ReviewQueueResponse getDailyQueue(
            @RequestParam(required = false) Long markdownFileId,
            @RequestParam(required = false, defaultValue = "Java") String category) {
        return reviewService.getDailyQueue(markdownFileId, category);
    }

    @GetMapping("/{topicId}/feedback-history")
    public List<FeedbackHistoryResponse> getFeedbackHistory(
            @PathVariable Long topicId) {
        return reviewService.getFeedbackHistory(topicId);
    }
}

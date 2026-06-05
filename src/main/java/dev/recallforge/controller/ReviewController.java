package dev.recallforge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.dto.AnswerRequest;
import dev.recallforge.dto.AnswerResponse;
import dev.recallforge.dto.DashboardResponse;
import dev.recallforge.dto.ReviewQuestionResponse;
import dev.recallforge.dto.ReviewQueueResponse;
import dev.recallforge.service.ReviewService;
import jakarta.validation.Valid;

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
     * and asks OpenAI to generate one question.
     */
    @PostMapping("/start")
    public ReviewQuestionResponse startReview(@RequestParam(required = false) Long markdownFileId) {
        return reviewService.startReview(markdownFileId);
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
            request.userAnswer()
        );
    }

    @GetMapping("/queue/today")
    public ReviewQueueResponse getDailyQueue(@RequestParam(required = false) Long markdownFileId) {
        return reviewService.getDailyQueue(markdownFileId);
    }
}

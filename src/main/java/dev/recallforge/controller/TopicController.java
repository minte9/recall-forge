package dev.recallforge.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.dto.ReviewHistoryResponse;
import dev.recallforge.dto.TopicResponse;
import dev.recallforge.service.ReviewService;
import dev.recallforge.service.TopicService;
import dev.recallforge.security.CurrentUserService;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
 
    private final TopicService topicService;
    private final ReviewService reviewService;
    private final CurrentUserService currentUserService;

    public TopicController(
        TopicService topicService, 
        ReviewService reviewService,
        CurrentUserService currentUserService
    ) {
        this.topicService = topicService;
        this.reviewService = reviewService;
        this.currentUserService = currentUserService;
    }

    /**
     * Show all topics ordered by weakest memory score first.
     */
    @GetMapping
    public List<TopicResponse> getTopics() {
        Long userId = currentUserService.currentUserId();

        return topicService.getAllTopics(userId)
                .stream()
                .map(TopicResponse::from)
                .toList();
    }

    @GetMapping("{topicId}/reviews")
    public List<ReviewHistoryResponse> gReviewHistory(
        @PathVariable Long topicId
    ) {
        return reviewService.getReviewHistoryForTopic(topicId);
    }

    @GetMapping("/due")
    public List<TopicResponse> getDueTopics(
            @RequestParam(required = false, defaultValue = "Java") String category) {
        Long userId = currentUserService.currentUserId();

        return topicService.getDueTopics(userId, category)
                .stream()
                .map(TopicResponse::from)
                .toList();
    }
}

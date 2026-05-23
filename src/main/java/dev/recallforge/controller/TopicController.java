package dev.recallforge.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.dto.ReviewHistoryResponse;
import dev.recallforge.dto.TopicResponse;
import dev.recallforge.service.ReviewService;
import dev.recallforge.service.TopicService;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
 
    private final TopicService topicService;
    private final ReviewService reviewService;

    public TopicController(TopicService topicService, ReviewService reviewService) {
        this.topicService = topicService;
        this.reviewService = reviewService;
    }

    /**
     * Import topics from README.md
     * 
     * Later, this will become:
     * POST /api/topics/upload
     */
    @PostMapping("/import-local")
    public List<TopicResponse> importLocalTopics() {
        return topicService.importTopics()
                .stream()
                .map(TopicResponse::from)
                .toList();
    }

    /**
     * Show all topics ordered by weakest memory score first.
     */
    @GetMapping
    public List<TopicResponse> getTopics() {
        return topicService.getAllTopics()
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
    public List<TopicResponse> getDueTopics() {
        return topicService.getDueTopics()
                .stream()
                .map(TopicResponse::from)
                .toList();
    }
}

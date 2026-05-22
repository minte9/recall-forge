package dev.recallforge.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.dto.TopicResponse;
import dev.recallforge.service.TopicService;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
 
    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
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
}

package dev.recallforge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.dto.RecallInsightResponse;
import dev.recallforge.service.RecallInsightService;

@RestController
@RequestMapping("/api/assistant")
public class RecallAssistantController {

    private final RecallInsightService insightService;

    public RecallAssistantController(
            RecallInsightService insightService) {
        this.insightService = insightService;
    }

    @GetMapping("/insight")
    public RecallInsightResponse getInsight(
            @RequestParam String category) {

        return insightService.getInsight(category);
    }
}
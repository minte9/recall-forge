package dev.recallforge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.recallforge.assistant.AssistantService;
import dev.recallforge.dto.RecallInsightResponse;
import dev.recallforge.service.RecallInsightService;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final RecallInsightService insightService;
    private final AssistantService assistantService;

    public AssistantController(
            RecallInsightService insightService,
            AssistantService assistantService) {
        this.insightService = insightService;
        this.assistantService = assistantService;
    }

    @GetMapping("/insight")
    public RecallInsightResponse getInsight(
            @RequestParam String category) {

        return insightService.getInsight(category);
    }

    @PostMapping("/ask")
    public AssistantResponse ask(@RequestBody AssistantRequest request) {

        String answer = assistantService.ask(
            request.question(),
            request.category()
        );

        return new AssistantResponse(answer);
    }

    public record AssistantRequest(
        String question,
        String category
    ) {}

    public record AssistantResponse(
        String answer
    ) {}
    
}
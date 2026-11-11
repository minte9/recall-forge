package dev.recallforge.service;

import org.springframework.stereotype.Service;

import dev.recallforge.domain.AiModel;
import dev.recallforge.dto.AiResponse;
import dev.recallforge.security.CurrentUserService;

@Service
public class AiModelRouter {
    
    private final OpenAiProvider openAiProvider;
    private final GeminiProvider geminiProvider;
    private final CurrentUserService currentUserService;

    public AiModelRouter(
            OpenAiProvider openAiProvider,
            GeminiProvider geminiProvider,
            CurrentUserService currentUserService) {

        this.openAiProvider = openAiProvider;
        this.geminiProvider = geminiProvider;
        this.currentUserService = currentUserService;
    }

    public AiResponse ask(String prompt, int maxTokens) {

        AiModel model = currentUserService
                .currentUser()
                .getAiModel();

        return switch (model) {
            case OPENAI -> 
                openAiProvider.ask(prompt, maxTokens);
            case GEMINI ->
                geminiProvider.ask(prompt, maxTokens);
        };
    }
}

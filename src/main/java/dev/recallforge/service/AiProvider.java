package dev.recallforge.service;

import dev.recallforge.dto.AiResponse;

public interface AiProvider {
    
    AiResponse ask(String prompt, int maxTokens);
}

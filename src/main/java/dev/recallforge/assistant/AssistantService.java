package dev.recallforge.assistant;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import dev.recallforge.security.CurrentUserService;

@Service
public class AssistantService {

    private final ChatClient chatClient;
    private final AssistantTools tools;
    private final CurrentUserService currentUser;

    public AssistantService(
            ChatModel chatModel,
            AssistantTools tools,
            CurrentUserService currentUser) {

        this.chatClient = ChatClient.builder(chatModel).build();
        this.tools = tools;
        this.currentUser = currentUser;
    }

    public String ask(String question, String category) {

        Long userId = currentUser.currentUserId();

        String scope = category == null || category.isBlank()
                ? "All categories"
                : category;

        return chatClient.prompt()
            .system("""
                You are RecallForge's AI Recall Assistant.

                Help users understand their mistakes and progress.

                Keep responses concise:
                - Maximum 100 words.
                - Use 1-2 short paragraphs.
                - Focus on actionable insights.
                - Avoid lengthy explanations.

                Use tools to retrieve actual user data.
                Never invent statistics or review results.

                Selected category: {category}
                """)
            .system(s -> s.param("category", scope))
            .user(question)
            .options(OpenAiChatOptions.builder()
                .maxCompletionTokens(250)
                .build())
            .tools(tools)
            .toolContext(Map.of("userId", userId))
            .call()
            .content();
    }
}
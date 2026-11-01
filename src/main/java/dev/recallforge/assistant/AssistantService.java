package dev.recallforge.assistant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import dev.recallforge.config.AppReviewLimitProperties;
import dev.recallforge.domain.QuestionGeneration;
import dev.recallforge.exception.DailyQuestionLimitExceededException;
import dev.recallforge.repository.QuestionGenerationRepository;
import dev.recallforge.security.CurrentUserService;

@Service
public class AssistantService {

    // Pseudo-category for question_generations rows, so assistant questions
    // count toward the per-user daily AI limit (same approach as optimize).
    static final String ASSISTANT_CATEGORY = "__assistant__";

    private final ChatClient chatClient;
    private final AssistantTools tools;
    private final CurrentUserService currentUser;
    private final QuestionGenerationRepository questionGenerationRepository;
    private final AppReviewLimitProperties reviewLimitProperties;

    public AssistantService(
            ChatModel chatModel,
            AssistantTools tools,
            CurrentUserService currentUser,
            QuestionGenerationRepository questionGenerationRepository,
            AppReviewLimitProperties reviewLimitProperties) {

        this.chatClient = ChatClient.builder(chatModel).build();
        this.tools = tools;
        this.currentUser = currentUser;
        this.questionGenerationRepository = questionGenerationRepository;
        this.reviewLimitProperties = reviewLimitProperties;
    }

    public String ask(String question, String category) {

        // The panel is admin-only in the UI; enforce it here too so the
        // endpoint can't be used to run OpenAI calls directly.
        currentUser.requireAdmin();

        Long userId = currentUser.currentUserId();

        ensureDailyAiLimit(userId);

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

    private void ensureDailyAiLimit(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        long aiCallsToday = questionGenerationRepository.countByUserIdAndGeneratedAtAfter(userId, startOfDay);

        if (aiCallsToday >= reviewLimitProperties.dailyUserQuestionLimit()) {
            throw new DailyQuestionLimitExceededException(
                    "You've hit your daily AI limit across all categories. Come back tomorrow for more.");
        }

        questionGenerationRepository.save(new QuestionGeneration(userId, ASSISTANT_CATEGORY));
    }
}

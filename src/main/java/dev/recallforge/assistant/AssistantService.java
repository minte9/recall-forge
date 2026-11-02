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

    @SuppressWarnings("null")
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

    @SuppressWarnings("null")
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

                When analyzing recall progress:
                - Use getProgressStatistics for historical performance.
                - Compare average scores and review counts separately.
                - A higher review count does not necessarily mean better recall.
                - Explain whether average scores increased or decreased.
                - Treat incomplete calendar months cautiously.
                - If either month has no reviews, do not claim improvement.
                - Do not invent statistics or trends.
                - Distinguish review performance from long-term retention.

                When comparing monthly progress:
                - Always mention the actual month names.
                - Use "so far in [month]" for the current month.
                - Explain score changes in percentage points.
                - Do not confuse increased review volume with improved performance.
                - If the current month has very few reviews, mention that the trend is preliminary.

                When analyzing knowledge areas:
                - Use getKnowledgeAreas to retrieve actual topic statistics.
                - Distinguish memory scores from review scores.
                - A high memory score indicates stronger estimated recall.
                - Mastered topics meet RecallForge's mastery criteria.
                - Identify strong and weak areas using the returned data.
                - Do not claim the user lacks knowledge of topics that
                are simply absent from RecallForge.
                - Do not invent categories, subcategories, or topic counts.
                - Explain that knowledge statistics reflect the user's
                recorded RecallForge topics, not their entire expertise.
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

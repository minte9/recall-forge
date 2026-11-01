package dev.recallforge.assistant;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

@Component
public class AssistantTools {

    private final AssistantAnalyticsService analytics;

    public AssistantTools(AssistantAnalyticsService analytics) {
        this.analytics = analytics;
    }

    @Tool(description = """
        Retrieve recent mistakes and low-scoring answers
        for a knowledge category. Use when the user asks
        about mistakes, weaknesses, or recurring errors.
        """)
    public Object getMistakes(
            @ToolParam(description = "Knowledge category, e.g. Java")
            String category,
            ToolContext context) {

        Long userId = (Long) context.getContext().get("userId");

        return analytics.getMistakes(userId, category);
    }
}
package dev.recallforge.assistant;

import java.util.List;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
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

    @Tool(description = """
        Retrieve the user's recent review history, including
        questions, answers, feedback, scores, and review dates.

        Use this tool when the user asks what they reviewed,
        wants to see past answers, or asks about recent
        review performance.

        Category is optional. Use an empty string to retrieve
        reviews across all categories.

        The maximum number of returned reviews is 10.
        """)
    public List<ReviewHistoryDto> getReviewHistory(
            @ToolParam(description = """
                Knowledge category such as Java or Spring Boot.
                Use an empty string for all categories.
                """)
            String category,

            @ToolParam(description = """
                Number of recent reviews to retrieve.
                Default to 5 when unspecified. Maximum 10.
                """)
            Integer limit,

            ToolContext context) {

        Long userId =
            (Long) context.getContext().get("userId");

        return analytics.getReviewHistory(
            userId,
            category,
            limit == null ? 5 : limit
        );
    }
}
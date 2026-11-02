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

    @Tool(description = """
        Retrieve the authenticated user's recall progress
        statistics for the current and previous calendar month.

        Returns review counts, average scores, high-score
        rates, and changes between the two periods.

        Use this tool when the user asks about progress,
        improvement, performance trends, or whether
        they are getting better at a knowledge category.

        An empty category means all categories.
    """)
    public ProgressStatisticsDto getProgressStatistics(
            @ToolParam(description = """
                Knowledge category, such as Java or Spring Boot.
                Use an empty string for all categories.
                """)
            String category,

            ToolContext context) {

        Long userId =
            (Long) context.getContext().get("userId");

        return analytics.getProgressStatistics(
            userId,
            category
        );
    }

    @Tool(description = """
        Retrieve the authenticated user's existing knowledge
        areas, grouped by category and subcategory.

        Returns topic counts, average memory scores,
        and mastered topic counts.

        Use this tool when the user asks:
        - What subjects or knowledge areas they already have
        - Which categories or subcategories are strongest
        - Which knowledge areas are weakest
        - How many topics they have in a category
        - What areas they should improve based on recall strength

        An empty category means all categories.

        This tool only analyzes existing knowledge.
        It does not identify missing curriculum topics.
    """)
    public List<KnowledgeAreaDto> getKnowledgeAreas(
            @ToolParam(description = """
                Knowledge category, such as Java or Spring Boot.
                Use an empty string for all categories.
                """)
            String category,

            ToolContext context) {

        Long userId =
            (Long) context.getContext().get("userId");

        return analytics.getKnowledgeAreas(
            userId,
            category
        );
    }
}
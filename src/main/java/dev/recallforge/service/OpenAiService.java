package dev.recallforge.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import dev.recallforge.dto.AiResponse;

/**
 * All prompt-building and response-parsing for OpenAI calls lives here:
 * {@link #generateQuestion} builds a review question from a topic (adjusting
 * difficulty by memory score and using recent reviews to stay focused on the
 * same concept for a few consecutive questions before moving to another),
 * and
 * {@link #evaluateAnswer} grades a submitted answer into a 0-1 score plus
 * feedback. {@link #generateCodeChallenge} and {@link #evaluateCodeChallenge}
 * are the code-focused counterparts used by the practice-only "Code
 * Challenge" flow. Called by {@link ReviewService}. {@link #optimizeTopicBodies}
 * and {@link #generateStructuredMarkdown} back the admin-only "Optimize"
 * markdown feature, called by {@link MarkdownFileService}. Uses the
 * {@code RestClient} bean configured in {@code config/OpenAiConfig}.
 */
@Service
public class OpenAiService {

    private final RestClient openAiRestClient;
    private final String model;

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);

    // Below this, the user is still shaky on the topic and gets recall-level questions.
    private static final double PROFICIENCY_THRESHOLD = 0.7;

    // Rewriting a whole file's worth of topics needs far more headroom than
    // the short question/grading calls above.
    private static final int OPTIMIZE_MAX_TOKENS = 2500;
    private static final int MAX_OPTIMIZE_INPUT_CHARS = 12000;

    public OpenAiService(RestClient openAiRestClient, @Value("${openai.model}") String model) {
        this.openAiRestClient = openAiRestClient;
        this.model = model;
    }

    public String generateQuestion(
            String topicTitle,
            String topicContent,
            double memoryScore,
            List<ReviewSummary> recentReviews) {
        String prompt = """
        Create one simple question to test understanding of this topic.

        Topic title:
        %s

        Topic content (a short paragraph, plus a code example whose comments
        are part of the explanation, not just syntax):
        %s

        User's proficiency on this topic: %s

        %s
        Rules:
        - Return only the question.
        - Keep it short (one sentence, 70-80 chars maximum).
        - Test understanding of the concept, not memorization of code syntax.
        - Do not include code snippets, code blocks, or inline code in the question itself.
        - Do not ask about the output of code.
        - Prefer conceptual or practical questions that can be answered in a few sentences.
        - The question must be answerable using only facts, reasoning, or explanations
          that are explicitly stated above, in either the paragraph or the code's comments.
          Do not require outside knowledge or reasoning that isn't spelled out there.
        - Before asking "why" or "how", confirm the content above actually explains that
          reason or mechanism. If it doesn't, ask a "what" or "when" question instead.
        - Phrase the question so its meaning is clear on first read - plain, direct
          wording over clever or layered phrasing. The user should spend their effort
          answering, not figuring out what is being asked.
        - For spaced repetition, the user should get 2-3 consecutive questions on the
          same concept before moving to another - use the recently asked questions
          listed above (most recent first) to judge which concept to continue or move
          on from.
        - Do not include the answer.
        """;
        prompt = prompt.formatted(
                topicTitle,
                trimTopicContent(topicContent),
                proficiencyGuidance(memoryScore),
                recentReviewsSection(recentReviews));

        AiResponse response = ask(prompt, 40);  // question generation
        return response.content();
    }

    /**
     * Generates a practice-only "Code Challenge" question in the topic's
     * own category (Java, Python, Git, SQL, ...): unlike
     * {@link #generateQuestion}, this one deliberately asks the user to
     * write code from scratch - no snippet is shown, the challenge is
     * worded only. Always graded strictly (see
     * {@link #evaluateCodeChallenge}) and treated as practice by the
     * caller, so it never affects the topic's spaced-repetition state.
     */
    public String generateCodeChallenge(
            String category, String topicTitle, String topicContent, double memoryScore,
            List<String> recentChallenges) {
        String prompt = """
        Create one %s coding challenge to test understanding of this topic.

        Topic title:
        %s

        Topic content (a short paragraph, plus a code example whose comments
        are part of the explanation, not just syntax):
        %s

        User's proficiency on this topic: %s

        %s
        Rules:
        - Ask the user to write %s code from scratch that applies the
          concept above - do not show any code in the challenge itself.
        - Prefer a real-life, practical scenario (a small task someone
          would actually write this code for) over an abstract or purely
          academic exercise, whenever the concept allows for one.
        - State exactly what the code must do, including any inputs and
          the expected result, so correctness is unambiguous.
        - Keep it short (1-2 sentences, 200 chars maximum).
        - Do not repeat any of the challenges already given for this topic,
          listed above - ask something meaningfully different.
        - Return only the challenge text, nothing else before or after.
        - Do not include the solution.
        """;
        prompt = prompt.formatted(
                category,
                topicTitle,
                trimTopicContent(topicContent),
                proficiencyGuidance(memoryScore),
                recentChallengesSection(recentChallenges),
                category);

        AiResponse response = ask(prompt, 100);  // code challenge generation
        return response.content();
    }

    private String recentChallengesSection(List<String> recentChallenges) {
        if (recentChallenges == null || recentChallenges.isEmpty()) {
            return "";
        }

        StringBuilder section = new StringBuilder(
            "Challenges already given for this topic, most recent first:\n");

        for (String challenge : recentChallenges) {
            section.append("- \"%s\"\n".formatted(trimText(challenge, 200)));
        }
        section.append("\n");

        return section.toString();
    }

    private String proficiencyGuidance(double memoryScore) {
        if (memoryScore < PROFICIENCY_THRESHOLD) {
            return "EASY - the user is still building basic understanding of this topic. "
                + "Ask a foundational, recall-oriented question about a single concept "
                + "explicitly stated above.";
        }

        return "MEDIUM - the user already has a solid grasp of this topic. "
            + "Ask a question that requires connecting two ideas, explaining a "
            + "relationship, or applying the concept, not just recalling a definition.";
    }

    private String recentReviewsSection(List<ReviewSummary> recentReviews) {
        if (recentReviews == null || recentReviews.isEmpty()) {
            return "";
        }

        StringBuilder section = new StringBuilder(
            "Recently asked questions on this topic, most recent first:\n");

        for (ReviewSummary review : recentReviews) {
            String feedbackSuffix = review.feedback() == null || review.feedback().isBlank()
                ? ""
                : ", feedback: " + trimText(review.feedback(), 200);

            section.append("- \"%s\" (score %.2f%s)\n".formatted(
                trimText(review.question(), 200),
                review.score(),
                feedbackSuffix));
        }
        section.append("\n");

        return section.toString();
    }

    public EvaluationResult evaluateAnswer(String topicTitle, String topicContent, String question, String userAnswer) {
        String prompt = """
        You are evaluating a learning review answer.

        Topic:
        %s

        Topic content:
        %s

        Question:
        %s

        User answer:
        %s

        Grade the answer from 0 to 1:
        - 1.0 = perfectly correct
        - 0.7 to 0.9 = mostly correct
        - 0.4 to 0.6 = partially correct
        - 0.1 to 0.3 = mostly incorrect but relevant
        - 0.0 = completely incorrect

        Ignore minor grammatical, spelling, or phrasing errors in the user's
        answer - grade and give feedback based on the correctness of the
        underlying understanding, not writing quality. 
        If the score is below 0.7, include the short correct answer (or a concise hint toward it) 
        as part of the feedback itself — one sentence, not a separate line.

        Return exactly this format:

        score: <number>
        feedback: <short explanation>
        """;
        prompt = prompt.formatted(topicTitle, topicContent, question, trimUserAnswer(userAnswer));

        AiResponse response = ask(prompt, 120);  // answer evaluation
        return parseEvaluation(response.content(), response.totalTokens());
    }

    /**
     * Grades a Code Challenge answer as strictly as a real compiler,
     * interpreter, or engine for the topic's category would: unlike
     * {@link #evaluateAnswer},
     * wording and phrasing are irrelevant - the submitted code must be
     * exactly correct and compile/run as required.
     */
    public EvaluationResult evaluateCodeChallenge(
            String category, String topicTitle, String topicContent, String question, String userAnswer) {
        String prompt = """
        You are grading a %s coding challenge as strictly as a real %s
        compiler, interpreter, or engine would - correctness only, wording
        does not matter.

        Topic:
        %s

        Topic content:
        %s

        Challenge:
        %s

        User's submitted code:
        %s

        Grade from 0 to 1:
        - 1.0 = the code compiles/runs and correctly does exactly what the
          challenge asked
        - 0.5 = compiles/runs but the logic or result is subtly wrong
        - 0.0 = does not compile/run, is not valid %s, or is fundamentally
          wrong

        Return exactly this format:

        score: <number>
        feedback: <one short sentence; if wrong, name the correct approach only>
        """;
        prompt = prompt.formatted(
                category,
                category,
                topicTitle,
                trimTopicContent(topicContent),
                question,
                trimUserAnswer(userAnswer),
                category);

        AiResponse response = ask(prompt, 90);  // code challenge evaluation
        return parseEvaluation(response.content(), response.totalTokens());
    }

    /**
     * Rewrites topic bodies only. Titles are never sent for the model to
     * echo back with room for drift - the caller matches the response's
     * "## " headings against its own known titles and splices bodies back
     * under them, so this only needs to instruct the model to keep them
     * unchanged and in place.
     */
    public String optimizeTopicBodies(List<TopicToOptimize> topics) {
        StringBuilder topicsBlock = new StringBuilder();

        for (TopicToOptimize topic : topics) {
            topicsBlock
                .append("## ").append(topic.title()).append("\n")
                .append(trimTopicContent(topic.content()))
                .append("\n\n");
        }

        String prompt = """
        Rewrite each topic below for a spaced-repetition recall app, not a
        knowledgebase article - each explanation must be readable in one
        glance, not teach the topic from scratch.

        Rules for each topic:
        - Explanation: 2-3 sentences, ~45-60 words. One main idea only.
        - Code example: short and focused; comments explain WHY, not WHAT.
        - Code lines (including trailing comments) must stay under 80
          characters.
        - Trim boilerplate (imports, full class ceremony) unless it is the
          point of the topic.
        - Do not change the topic titles below (the "## " heading lines) -
          copy each one back exactly as given, character for character.
        - Do not add, remove, split, or merge topics - return exactly the
          same number of topics, in the same order.
        - Return the result in the same "## <title>" format shown below,
          one topic per section, nothing else before or after.

        Topics:
        %s
        """;
        prompt = prompt.formatted(topicsBlock.toString().trim());

        AiResponse response = ask(prompt, OPTIMIZE_MAX_TOKENS);
        return response.content();
    }

    /**
     * Turns unstructured raw notes into a fully structured markdown file,
     * header included. {@code allowedEnvironments} grounds the Environment
     * choice against the curated list so the model prefers a known bucket
     * over inventing a new one.
     */
    public String generateStructuredMarkdown(String rawContent, List<EnvironmentOption> allowedEnvironments) {
        String environmentsBlock = allowedEnvironments.stream()
            .map(option -> "  - %s: %s".formatted(option.name(), option.description()))
            .collect(Collectors.joining("\n"));

        String prompt = """
        Turn the raw notes below into a properly structured markdown file
        for a spaced-repetition recall app.

        Format exactly:
        # <Environment> / <Category> / <Subcategory> / <File Title>

        ## 1. <Topic 1 Title>
        <2-3 sentence explanation, ~45-60 words>
        ```<language>
        <short, focused code example, comments explain WHY not WHAT,
        lines under 80 characters>
        ```

        ## 2. <Topic 2 Title>
        ...

        Rules:
        - Number each topic title, starting at 1, in the order they
          appear ("## 1. <Title>", "## 2. <Title>", etc.) - the number
          is part of the heading text itself, not separate from it.
        - 2 to 5 topics total. Only include topics that are genuinely
          important - do not pad to hit a target count.
        - One main idea per topic, readable in one glance, not a full
          tutorial.
        - Code lines (including trailing comments) must stay under 80
          characters.
        - For <Environment>, choose exactly one from this list unless truly
          nothing fits:
        %s
        - The implementation language is not always the right environment.
          If the content is fundamentally about a specific domain (machine
          learning, databases, security, cloud, mobile, etc.) rather than
          about the language itself, choose that domain environment even
          when the code example happens to be written in a general-purpose
          language like Python or Java. Only choose the language
          environment when the content is teaching the language/framework
          itself, not a specialized field built on top of it. For example,
          code using scikit-learn, pandas/numpy for analysis, TensorFlow,
          PyTorch, or algorithms like KNN/regression/classification/
          clustering is AI content, not Python content, even though it is
          written in Python syntax and imports ordinary Python libraries.
        - Only propose a new environment if none of the above are a
          reasonable fit; if you do, it must still read like a short
          ecosystem name (2-3 words max), not a sentence.
        - Category and Subcategory should be short, specific, and
          consistent with how a technical curriculum would organize this
          content.

        Raw notes:
        %s
        """;
        prompt = prompt.formatted(environmentsBlock, trimText(rawContent, MAX_OPTIMIZE_INPUT_CHARS));

        AiResponse response = ask(prompt, OPTIMIZE_MAX_TOKENS);
        return response.content();
    }

    private AiResponse ask(String prompt, int maxTokens) {
        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", new Object[]{
                Map.of(
                    "role", "user", "content", prompt
                )
            },
            "temperature", 0.3,
            "max_tokens", maxTokens  // 100 tokens ≈ 75 words / gpt-4o-mini ~ 66k = $0.01
        );

        JsonNode response = openAiRestClient
                .post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("OpenAI returned an empty response.");
        }

        JsonNode contentNode = response
                .path("choices")
                .path(0)
                .path("message")
                .path("content");

        if (contentNode.isMissingNode()) {
            throw new IllegalStateException("Could not read OpenAI response: " + response);
        }

        JsonNode usage = response.path("usage");
        int promptTokens = usage.path("prompt_tokens").asInt();
        int completionTokens = usage.path("completion_tokens").asInt();
        int totalTokens = usage.path("total_tokens").asInt();

        String content = contentNode.asText().trim();
        
        log.info(
            "OpenAI tokens: prompt={}, completion={}, total={}",  // docker compose logs -f app | grep "OpenAI tokens"
            promptTokens,
            completionTokens,
            totalTokens
        ); 
        
        //log.debug("OpenAI prompt: {}", prompt);  // docker compose logs -f app | grep -A 40 "OpenAI prompt"

        return new AiResponse(
            content,
            promptTokens,
            completionTokens,
            totalTokens
        );
    }

    private EvaluationResult parseEvaluation(String text, int totalTokens) {
        double score = 0.5;
        String feedback = text;

        String[] lines = text.split("\\R");

        for (String line : lines) {
            String normalized = line.trim().toLowerCase();

            if (normalized.startsWith("score:")) {
                String value = line.substring(line.indexOf(":") + 1).trim();

                try {
                    score = Double.parseDouble(value);
                } catch (NumberFormatException ignored) {
                    score = 0.5;
                }
            }

            if (normalized.startsWith("feedback:")) {
                feedback = line.substring(line.indexOf(":") + 1).trim();
            }
        }

        score = Math.max(0.0, Math.min(1.0, score));

        return new EvaluationResult(score, feedback, totalTokens);
    }

    private String trimTopicContent(String content) {
        int maxChars = 3000;

        if (content == null) {
            return "";
        }

        String trimmed = content.trim();
        if (trimmed.length() <= maxChars) {
            return trimmed;
        }

        return trimmed.substring(0, maxChars) + "\n...[truncated]";
    }

    private String trimText(String text, int maxChars) {
        if (text == null) {
            return "";
        }

        String trimmed = text.trim();
        return trimmed.length() <= maxChars ? trimmed : trimmed.substring(0, maxChars);
    }

    private String trimUserAnswer(String answer) {
        int maxChars = 2000;

        if (answer == null) {
            return "";
        }

        String trimmed = answer.trim();
        if (trimmed.length() <= maxChars) {
            return trimmed;
        }

        return trimmed.substring(0, maxChars);
    }
}

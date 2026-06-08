package dev.recallforge.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MarkdownTopicImporter {

    private static final String DEFAULT_ENVIRONMENT = "General";
    private static final String DEFAULT_CATEGORY = "Uncategorized";
    private static final String DEFAULT_SUBCATEGORY = "Uncategorized";
    private static final String DEFAULT_FILETITLE = "Untitled";

    /**
     * Parse markdown into topics.
     * Every markdown heading starts a new topic:
     * - supports headings from # to ######
     * - everything until the next heading becomes content
     */
    public List<ParsedTopic> parseMarkdown(String markdown) {
        List<ParsedTopic> topics = new ArrayList<>();

        String environment = DEFAULT_ENVIRONMENT;
        String category = DEFAULT_CATEGORY;
        String subcategory = DEFAULT_SUBCATEGORY;
        String fileTitle = DEFAULT_FILETITLE;

        String currentTitle = null;
        StringBuilder currentContent = new StringBuilder();

        for (String line : markdown.split("\\R")) {
            boolean isPathHeading = line.matches("^##\\s+.+");
            boolean isTopicHeading = line.matches("^###\\s+.+");

            if (isPathHeading) {
                String path = line.replaceFirst("^##\\s+", "").trim();

                String[] parts = path.split("\\s*/\\s*");

                environment = getPartOrDefault(parts, 0, DEFAULT_ENVIRONMENT);
                category = getPartOrDefault(parts, 1, DEFAULT_CATEGORY);
                subcategory = getPartOrDefault(parts, 2, DEFAULT_SUBCATEGORY);
                fileTitle = getPartOrDefault(parts, 3, DEFAULT_FILETITLE);

                continue;
            }

            if (isTopicHeading) {
                if (currentTitle != null) {
                    String content = currentContent.toString().trim();

                    if (!content.isBlank()) {
                        topics.add(new ParsedTopic(
                                environment,
                                category,
                                subcategory,
                                fileTitle,
                                currentTitle,
                                content
                        ));
                    }
                }

                currentTitle = line.replaceFirst("^###\\s+", "").trim();

                if (fileTitle.equals(DEFAULT_FILETITLE)) {  // If no file title, use first topic title
                    fileTitle = currentTitle;
                }
                
                currentContent = new StringBuilder();

            } else if (currentTitle != null) {
                currentContent.append(line).append("\n");
            }
        }

        if (currentTitle != null) {
            String content = currentContent.toString().trim();

            if (!content.isBlank()) {
                topics.add(new ParsedTopic(
                        environment,
                        category,
                        subcategory,
                        fileTitle,
                        currentTitle,
                        content
                ));
            }
        }

        // If no ### topics exist, save whole markdown as one topic
        if (topics.isEmpty()) {
            String content = markdown.trim();

            if (!content.isBlank()) {
                topics.add(new ParsedTopic(
                    environment,
                    category,
                    subcategory,
                    fileTitle,
                    fileTitle,
                    content
                ));
            }
        }

        return topics;
    }

    private String getPartOrDefault(String[] parts, int index, String defaultValue) {
        if (parts.length <= index || parts[index].isBlank()) {
            return defaultValue;
        }

        return parts[index].trim();
    }

    public record ParsedTopic(
            String environment,
            String category,
            String subcategory,
            String fileTitle,
            String title,
            String content
    ) {
    }
}
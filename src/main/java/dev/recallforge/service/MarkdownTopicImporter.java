package dev.recallforge.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MarkdownTopicImporter {

    /**
     * Parse markdown into topics.
     * Every markdown heading starts a new topic:
     * - supports headings from # to ######
     * - everything until the next heading becomes content
     */
    public List<ParsedTopic> parseMarkdown(String markdown) {
        List<ParsedTopic> topics = new ArrayList<>();

        String environment = null;
        String category = null;
        String subcategory = null;
        String fileTitle = null;

        String currentTitle = null;
        StringBuilder currentContent = new StringBuilder();

        for (String line : markdown.split("\\R")) {
            boolean isPathHeading = line.matches("^##\\s+.+");
            boolean isTopicHeading = line.matches("^###\\s+.+");

            if (isPathHeading) {
                String path = line.replaceFirst("^##\\s+", "").trim();

                String[] parts = path.split("\\s*/\\s*");

                environment = parts.length > 0 ? parts[0] : "General";
                category = parts.length > 1 ? parts[1] : "General";
                subcategory = parts.length > 2 ? parts[2] : "General";
                fileTitle = parts.length > 3 ? parts[3] : "General";

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

        return topics;
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
package dev.recallforge.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MarkdownTopicImporter {


    public record ParsedTopic(String title, String content) {
    }

    /**
     * Parse markdown into topics.
     * Every markdown heading starts a new topic:
     * - supports headings from # to ######
     * - everything until the next heading becomes content
     */
    public List<ParsedTopic> parseMarkdown(String markdown) {

        List<ParsedTopic> topics = new ArrayList<>();

        String currentTitle = null;
        StringBuilder currentContent = new StringBuilder();

        for (String line : markdown.split("\\R")) {

            // Match markdown headings
            boolean isHeading = line.matches("^#{1,6}\\s+.+");

            if (isHeading) {

                // Save previous topic before starting a new one
                if (currentTitle != null) {

                    String content = currentContent.toString().trim();

                    if (!content.isBlank()) {
                        topics.add(new ParsedTopic(
                                currentTitle,
                                content
                        ));
                    }
                }

                // Remove leading # characters and spaces.
                currentTitle =
                        line.replaceFirst("^#+\\s+", "").trim();

                // Start collecting content for the new topic.
                currentContent = new StringBuilder();

            } else if (currentTitle != null) {

                // Add body lines until the next heading appears.
                currentContent
                        .append(line)
                        .append("\n");
            }
        }

        // Save the final topic after loop ends.
        if (currentTitle != null) {

            String content = currentContent.toString().trim();

            if (!content.isBlank()) {
                topics.add(new ParsedTopic(
                        currentTitle,
                        content
                ));
            }
        }

        return topics;
    }
}
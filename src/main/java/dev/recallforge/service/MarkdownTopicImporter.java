package dev.recallforge.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Parses raw markdown text into a list of {@link ParsedTopic} records.
 *
 * This is a stateless parser with no persistence or per-user logic; it does
 * not read or write any files itself. It is used by
 * {@link MarkdownFileService} to turn uploaded/imported markdown content
 * into topics before they are saved for the current user. {@code #}
 * headings set the environment/category/subcategory/file title path, and
 * each {@code ##} heading starts a new topic whose content runs until the
 * next heading. Lines inside a fenced code block ({@code ```}/{@code ~~~})
 * are never treated as headings, even if they start with {@code #} (e.g. a
 * Python/Bash/YAML comment) - otherwise a comment line in example code
 * would be misparsed as a new file heading.
 */
@Service
public class MarkdownTopicImporter {

    private static final String DEFAULT_ENVIRONMENT = "General";
    private static final String DEFAULT_CATEGORY = "Uncategorized";
    private static final String DEFAULT_SUBCATEGORY = "Uncategorized";
    private static final String DEFAULT_FILETITLE = "Untitled";

    public List<ParsedTopic> parseMarkdown(String markdown) {
        List<ParsedTopic> topics = new ArrayList<>();

        String environment = DEFAULT_ENVIRONMENT;
        String category = DEFAULT_CATEGORY;
        String subcategory = DEFAULT_SUBCATEGORY;
        String fileTitle = DEFAULT_FILETITLE;

        String currentTitle = null;
        StringBuilder currentContent = new StringBuilder();
        boolean insideFence = false;

        for (String line : markdown.split("\\R")) {
            boolean isFenceDelimiter = line.matches("^\\s*(`{3,}|~{3,}).*");
            boolean isPathHeading = !insideFence && !isFenceDelimiter && line.matches("^#\\s+\\S.*");
            boolean isTopicHeading = !insideFence && !isFenceDelimiter && line.matches("^##\\s+\\S.*");

            if (isFenceDelimiter) {
                insideFence = !insideFence;
            }

            if (isPathHeading) {
                String path = line.replaceFirst("^#\\s+", "").trim();

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

                currentTitle = line.replaceFirst("^##\\s+", "").trim();

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

        // If no ## topics exist, save whole markdown as one topic
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
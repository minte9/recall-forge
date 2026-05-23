package dev.recallforge.service;

import dev.recallforge.domain.Topic;
import dev.recallforge.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class MarkdownTopicImporter {

    private final TopicRepository topicRepository;
    private final Path notesPath;

    public MarkdownTopicImporter(
            TopicRepository topicRepository, 
            @Value("${recallforge.notes.path}") String notesPath
    ) {
        this.topicRepository = topicRepository;
        this.notesPath = Path.of(notesPath);
    }

    private record ParsedTopic(String title, String content) {
    }

    /**
     * Imports topics from the configured markdown file:
     * - existing topics are matched by title and updated
     * - new topics are created when no existing title is found
     */
    public List<Topic> importFromLocalMarkdownFile() {
        String markdown = readMarkdownFile();

        List<ParsedTopic> parsedTopics = parseMarkdown(markdown);
        List<Topic> savedTopics = new ArrayList<>();

        for (ParsedTopic parsedTopic : parsedTopics) {
            Topic topic = topicRepository
                    .findByTitle(parsedTopic.title())
                    .map(existingTopic -> {
                        existingTopic.updateContent(parsedTopic.content());
                        return existingTopic;
                    })
                    .orElseGet(() -> 
                        new Topic(parsedTopic.title(), parsedTopic.content())
                    );

            savedTopics.add(
                topicRepository.save(topic)  // handles both insert and update
            );
        }

        return savedTopics;
    }

    private String readMarkdownFile() {
        try {
            return Files.readString(notesPath);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read markdown file: " + notesPath, e);
        }
    }

    /**
     * Parse markdown into topics.
     * Every markdown heading starts a new topic:
     * - supports headings from # to ######
     * - everything until the next heading becomes content
     */
    private List<ParsedTopic> parseMarkdown(String markdown) {

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
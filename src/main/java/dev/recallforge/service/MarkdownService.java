package dev.recallforge.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.Topic;
import dev.recallforge.exception.MarkdownAlreadyImportedException;
import dev.recallforge.repository.MarkdownFileRepository;
import dev.recallforge.repository.TopicRepository;

@Service
public class MarkdownService {

    private final MarkdownFileRepository markdownFileRepository;
    private final TopicRepository topicRepository;
    private final MarkdownTopicImporter markdownTopicImporter;

    public MarkdownService(
            MarkdownFileRepository markdownFileRepository,
            TopicRepository topicRepository,
            MarkdownTopicImporter markdownTopicImporter
    ) {
        this.markdownFileRepository = markdownFileRepository;
        this.topicRepository = topicRepository;
        this.markdownTopicImporter = markdownTopicImporter;
    }

    public void upload(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String contentHash = sha256(content);

        if (markdownFileRepository.existsByContentHash(contentHash)) {
            throw new MarkdownAlreadyImportedException(
                "This markdown content was already uploaded."
            );
        }

        MarkdownFile markdownFile = new MarkdownFile(filename, content, contentHash);
        markdownFileRepository.save(markdownFile);

        createTopicsFromMarkdown(markdownFile, content);
    }

    private void createTopicsFromMarkdown(MarkdownFile markdownFile, String markdown) {
        List<MarkdownTopicImporter.ParsedTopic> parseTopics = 
            markdownTopicImporter.parseMarkdown(markdown);

        for (MarkdownTopicImporter.ParsedTopic parsedTopic : parseTopics) {
            Topic topic = new Topic(
                parsedTopic.environment(),
                parsedTopic.category(),
                parsedTopic.subcategory(),
                parsedTopic.fileTitle(),
                parsedTopic.title(),
                parsedTopic.content(),
                markdownFile
            );

            topicRepository.save(topic);
        }
    }

    private String sha256(String content) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] bytes = md.digest(content.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
package dev.recallforge.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.Topic;
import dev.recallforge.dto.MarkdownUploadResponse;
import dev.recallforge.repository.MarkdownFileRepository;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.security.CurrentUserService;
import dev.recallforge.domain.UserAccount;
import jakarta.transaction.Transactional;

@Service
public class MarkdownFileService {

    private final MarkdownFileRepository markdownFileRepository;
    private final TopicRepository topicRepository;
    private final MarkdownTopicImporter markdownTopicImporter;
    private final CurrentUserService currentUserService;

    private static final long MAX_FILE_SIZE = 200 * 1024;  // 200 KB
    private static final int MAX_CONTENT_LENGTH = 200_000;
    private static final int MAX_TOPICS_PER_FILE = 100;

    public MarkdownFileService(
            MarkdownFileRepository markdownFileRepository,
            TopicRepository topicRepository,
            MarkdownTopicImporter markdownTopicImporter,
            CurrentUserService currentUserService
    ) {
        this.markdownFileRepository = markdownFileRepository;
        this.topicRepository = topicRepository;
        this.markdownTopicImporter = markdownTopicImporter;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public MarkdownUploadResponse upload(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.toLowerCase().endsWith(".md")) {
            throw new IllegalArgumentException("Only .md files are allowed.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File is too large. Maximum size is 200 KB.");
        }

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        return importMarkdown(content, sanitizeFilename(filename));
    }

    @Transactional
    public MarkdownUploadResponse importMarkdown(String content) throws Exception {
        return importMarkdown(content, null);
    }

    private MarkdownUploadResponse importMarkdown(String content, String filename) throws Exception {
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Markdown content is too large.");
        }

        String contentHash = sha256(content);

        Long userId = currentUserService.currentUserId();
        UserAccount user = currentUserService.currentUser();

        List<MarkdownTopicImporter.ParsedTopic> parsedTopics =
                markdownTopicImporter.parseMarkdown(content);

        if (parsedTopics.isEmpty()) {
            throw new IllegalArgumentException("Markdown file does not contain any topics.");
        }

        if (parsedTopics.size() > MAX_TOPICS_PER_FILE) {
            throw new IllegalArgumentException("Too many topics in one markdown file.");
        }

        MarkdownTopicImporter.ParsedTopic firstTopic = parsedTopics.get(0);

        Optional<MarkdownFile> existingFile = findExistingMarkdownFile(firstTopic, userId);

        MarkdownFile markdownFile;

        if (existingFile.isPresent()) {
            markdownFile = existingFile.get();

            markdownFile.updateContent(content, contentHash);
            markdownFile.updateMetadata(
                    firstTopic.environment(),
                    firstTopic.category(),
                    firstTopic.subcategory(),
                    firstTopic.fileTitle()
            );

            markdownFileRepository.save(markdownFile);

        } else {
            markdownFile = new MarkdownFile(
                    filename != null ? filename : sanitizeFilename(firstTopic.fileTitle() + ".md"),
                    content,
                    contentHash,
                    firstTopic.environment(),
                    firstTopic.category(),
                    firstTopic.subcategory(),
                    firstTopic.fileTitle(),
                    user
            );

            markdownFileRepository.save(markdownFile);
        }

        syncTopicsFromMarkdown(markdownFile, parsedTopics, userId, user);

        return new MarkdownUploadResponse(
                markdownFile.getId(),
                markdownFile.getFilename(),
                markdownFile.getContent()
        );
    }

    private String sanitizeFilename(String filename) {
        return filename
            .replace("\\", "")
            .replace("/", "")
            .replace("..", "")
            .trim();
    }

    private Optional<MarkdownFile> findExistingMarkdownFile(
        MarkdownTopicImporter.ParsedTopic firstTopic,
        Long userId
    ) {
        Optional<MarkdownFile> byMetadata =
                markdownFileRepository.findByEnvironmentAndCategoryAndSubcategoryAndTopicGroupAndUserId(
                        firstTopic.environment(),
                        firstTopic.category(),
                        firstTopic.subcategory(),
                        firstTopic.fileTitle(),
                        userId
                );

        if (byMetadata.isPresent()) {
            return byMetadata;
        }

        Optional<Topic> byTopicTitle =
                topicRepository.findByTitleAndUserId(firstTopic.title(), userId);

        return byTopicTitle.map(Topic::getMarkdownFile);
    }

    private void syncTopicsFromMarkdown(
        MarkdownFile markdownFile,
        List<MarkdownTopicImporter.ParsedTopic> parsedTopics,
        Long userId,
        UserAccount user
    ) {
        for (MarkdownTopicImporter.ParsedTopic parsedTopic : parsedTopics) {

            Optional<Topic> existingTopic =
                topicRepository.findByMarkdownFileAndTitleAndUserId(
                    markdownFile,
                    parsedTopic.title(),
                    userId
                );

            if (existingTopic.isPresent()) {
                Topic topic = existingTopic.get();

                topic.updateFromMarkdown(
                    parsedTopic.environment(),
                    parsedTopic.category(),
                    parsedTopic.subcategory(),
                    parsedTopic.fileTitle(),
                    parsedTopic.content(),
                    markdownFile
                );

                topicRepository.save(topic);
                continue;
            }

            Topic topic = new Topic(
                    parsedTopic.environment(),
                    parsedTopic.category(),
                    parsedTopic.subcategory(),
                    parsedTopic.fileTitle(),
                    parsedTopic.title(),
                    parsedTopic.content(),
                    markdownFile,
                    user
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
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
import dev.recallforge.exception.MarkdownAlreadyImportedException;
import dev.recallforge.exception.MarkdownFileHasReviewsException;
import dev.recallforge.repository.MarkdownFileRepository;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.security.CurrentUserService;
import dev.recallforge.domain.UserAccount;
import jakarta.transaction.Transactional;

/**
 * Imports and manages markdown files for the current user.
 *
 * Accepts uploaded {@code .md} files (or raw markdown content, e.g. from
 * {@link StarterPackService}), parses their topics via
 * {@link MarkdownTopicImporter}, and persists them as {@link MarkdownFile}
 * and {@link Topic} records scoped to {@link CurrentUserService#currentUser()}.
 * Files are matched against existing records by content hash or by
 * environment/category/subcategory/title metadata so re-imports update the
 * existing file and its topics instead of creating duplicates.
 */
@Service
public class MarkdownFileService {

    private final MarkdownFileRepository markdownFileRepository;
    private final TopicRepository topicRepository;
    private final ReviewRepository reviewRepository;
    private final MarkdownTopicImporter markdownTopicImporter;
    private final CurrentUserService currentUserService;

    private static final long MAX_FILE_SIZE = 200 * 1024;  // 200 KB
    private static final int MAX_CONTENT_LENGTH = 200_000;
    private static final int MAX_TOPICS_PER_FILE = 100;

    public MarkdownFileService(
            MarkdownFileRepository markdownFileRepository,
            TopicRepository topicRepository,
            ReviewRepository reviewRepository,
            MarkdownTopicImporter markdownTopicImporter,
            CurrentUserService currentUserService
    ) {
        this.markdownFileRepository = markdownFileRepository;
        this.topicRepository = topicRepository;
        this.reviewRepository = reviewRepository;
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

    /**
     * Imports starter-pack content, carrying over the template file's
     * display order verbatim (rather than auto-assigning one, as manual
     * uploads do).
     */
    @Transactional
    public MarkdownUploadResponse importMarkdown(String content, Integer displayOrder) throws Exception {
        return importMarkdown(content, null, false, displayOrder);
    }

    public MarkdownUploadResponse getById(Long markdownFileId) {
        Long userId = currentUserService.currentUserId();

        MarkdownFile markdownFile = markdownFileRepository.findByIdAndUserId(markdownFileId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Markdown file not found: " + markdownFileId));

        return new MarkdownUploadResponse(
                markdownFile.getId(),
                markdownFile.getFilename(),
                markdownFile.getContent()
        );
    }

    @Transactional
    public MarkdownUploadResponse update(Long markdownFileId, String content) throws Exception {
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Markdown content is too large.");
        }

        Long userId = currentUserService.currentUserId();
        UserAccount user = currentUserService.currentUser();

        MarkdownFile markdownFile = markdownFileRepository.findByIdAndUserId(markdownFileId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Markdown file not found: " + markdownFileId));

        List<MarkdownTopicImporter.ParsedTopic> parsedTopics =
                markdownTopicImporter.parseMarkdown(content);

        if (parsedTopics.isEmpty()) {
            throw new IllegalArgumentException("Markdown file does not contain any topics.");
        }

        if (parsedTopics.size() > MAX_TOPICS_PER_FILE) {
            throw new IllegalArgumentException("Too many topics in one markdown file.");
        }

        String contentHash = sha256(content);

        if (!contentHash.equals(markdownFile.getContentHash())) {
            Optional<MarkdownFile> duplicate = markdownFileRepository.findByContentHashAndUserId(contentHash, userId);

            if (duplicate.isPresent() && !markdownFileId.equals(duplicate.get().getId())) {
                throw new MarkdownAlreadyImportedException("This content already exists in another markdown file.");
            }
        }

        markdownFile.updateContent(content, contentHash);
        markdownFileRepository.save(markdownFile);

        syncTopicsFromMarkdown(markdownFile, parsedTopics, userId, user);

        return new MarkdownUploadResponse(
                markdownFile.getId(),
                markdownFile.getFilename(),
                markdownFile.getContent()
        );
    }

    private MarkdownUploadResponse importMarkdown(String content, String filename) throws Exception {
        return importMarkdown(content, filename, true, null);
    }

    private MarkdownUploadResponse importMarkdown(
            String content,
            String filename,
            boolean autoAssignDisplayOrder,
            Integer explicitDisplayOrder
    ) throws Exception {
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

            if (!autoAssignDisplayOrder) {
                markdownFile.updateDisplayOrder(explicitDisplayOrder);
            }

            markdownFileRepository.save(markdownFile);

        } else {
            Integer resolvedDisplayOrder = autoAssignDisplayOrder
                    ? nextDisplayOrder(userId, firstTopic.category(), firstTopic.subcategory())
                    : explicitDisplayOrder;

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
            markdownFile.updateDisplayOrder(resolvedDisplayOrder);

            markdownFileRepository.save(markdownFile);
        }

        syncTopicsFromMarkdown(markdownFile, parsedTopics, userId, user);

        return new MarkdownUploadResponse(
                markdownFile.getId(),
                markdownFile.getFilename(),
                markdownFile.getContent()
        );
    }

    @Transactional
    public long countReviews(Long markdownFileId) {
        Long userId = currentUserService.currentUserId();

        markdownFileRepository.findByIdAndUserId(markdownFileId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Markdown file not found: " + markdownFileId));

        return reviewRepository.countByTopicMarkdownFileIdAndUserId(markdownFileId, userId);
    }

    @Transactional
    public void delete(Long markdownFileId) {
        Long userId = currentUserService.currentUserId();

        MarkdownFile markdownFile = markdownFileRepository.findByIdAndUserId(markdownFileId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Markdown file not found: " + markdownFileId));

        long reviewCount = reviewRepository.countByTopicMarkdownFileIdAndUserId(markdownFileId, userId);

        if (reviewCount > 0) {
            throw new MarkdownFileHasReviewsException(
                    "Reset reviews for this markdown file before deleting it.");
        }

        topicRepository.deleteByMarkdownFileIdAndUserId(markdownFileId, userId);
        markdownFileRepository.delete(markdownFile);
    }

    private Integer nextDisplayOrder(Long userId, String category, String subcategory) {
        return markdownFileRepository
                .findMaxDisplayOrderByUserIdAndCategoryAndSubcategory(userId, category, subcategory)
                .map(max -> max + 1)
                .orElse(1);
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

        deleteStaleTopics(markdownFile, parsedTopics, userId);
    }

    /**
     * Removes topics that no longer have a matching heading in this markdown
     * file (e.g. the heading was renamed on re-import), along with their
     * reviews. Renaming a topic's title is treated the same as deleting it
     * and creating a new one, so its review history is intentionally not
     * preserved.
     */
    private void deleteStaleTopics(
        MarkdownFile markdownFile,
        List<MarkdownTopicImporter.ParsedTopic> parsedTopics,
        Long userId
    ) {
        List<String> currentTitles = parsedTopics.stream()
                .map(MarkdownTopicImporter.ParsedTopic::title)
                .toList();

        List<Topic> staleTopics = topicRepository.findByMarkdownFileAndUserIdAndTitleNotIn(
                markdownFile,
                userId,
                currentTitles
        );

        if (staleTopics.isEmpty()) {
            return;
        }

        List<Long> staleTopicIds = staleTopics.stream().map(Topic::getId).toList();

        reviewRepository.deleteByTopicIdInAndUserId(staleTopicIds, userId);
        topicRepository.deleteAll(staleTopics);
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
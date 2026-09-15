package dev.recallforge.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.Review;
import dev.recallforge.domain.RetryType;
import dev.recallforge.domain.Topic;
import dev.recallforge.dto.AppliedOptimization;
import dev.recallforge.dto.CodeChallengeResponse;
import dev.recallforge.dto.MarkdownUploadResponse;
import dev.recallforge.dto.MarkdownVaultUploadResponse;
import dev.recallforge.exception.MarkdownAlreadyImportedException;
import dev.recallforge.exception.MarkdownFileHasReviewsException;
import dev.recallforge.repository.AllowedEnvironmentRepository;
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
 *
 * {@link #optimize} is a separate, admin-only, non-persisting entry point
 * that returns AI-rewritten markdown text for preview before the caller
 * submits it (if at all) through the normal {@link #update}/
 * {@link #createFromContent} paths.
 */
@Service
public class MarkdownFileService {

    private final MarkdownFileRepository markdownFileRepository;
    private final TopicRepository topicRepository;
    private final ReviewRepository reviewRepository;
    private final MarkdownTopicImporter markdownTopicImporter;
    private final CurrentUserService currentUserService;
    private final OpenAiService openAiService;
    private final AllowedEnvironmentRepository allowedEnvironmentRepository;

    private static final long MAX_FILE_SIZE = 200 * 1024;  // 200 KB
    private static final int MAX_CONTENT_LENGTH = 200_000;
    private static final int MAX_TOPICS_PER_FILE = 100;
    private static final int MAX_VAULT_FILES = 300;

    // Obsidian's "Create new vault" dialog suggests this exact name by
    // default, and many users keep it - if the picked folder is named
    // this, it's very likely the whole vault got selected instead of a
    // single environment folder inside it. Not exhaustive (a renamed
    // vault won't be caught), just the common case.
    private static final String LIKELY_VAULT_ROOT_NAME = "obsidian vault";

    public MarkdownFileService(
            MarkdownFileRepository markdownFileRepository,
            TopicRepository topicRepository,
            ReviewRepository reviewRepository,
            MarkdownTopicImporter markdownTopicImporter,
            CurrentUserService currentUserService,
            OpenAiService openAiService,
            AllowedEnvironmentRepository allowedEnvironmentRepository
    ) {
        this.markdownFileRepository = markdownFileRepository;
        this.topicRepository = topicRepository;
        this.reviewRepository = reviewRepository;
        this.markdownTopicImporter = markdownTopicImporter;
        this.currentUserService = currentUserService;
        this.openAiService = openAiService;
        this.allowedEnvironmentRepository = allowedEnvironmentRepository;
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
     * Admin-only. Imports an Obsidian vault: many files, each with a path
     * relative to the vault root (as sent by a folder/webkitdirectory file
     * picker) - {@code paths.get(i)} is the relative path of {@code
     * files.get(i)}.
     *
     * A file whose content already has a "#" header line is imported
     * exactly like a normal upload - an explicit in-file header always
     * wins. Otherwise a header is synthesized from the file's folder path
     * (see {@link #synthesizeHeaderFromPath}) before it's handed to the
     * same {@link #importMarkdown} used by single-file uploads. Non-{@code
     * .md} files (images, {@code .obsidian/} config, etc.) are silently
     * skipped rather than treated as errors.
     *
     * One bad markdown file (too large, no topics, too many topics) is
     * recorded in the response's {@code errors} list and skipped rather
     * than aborting the rest of the vault.
     *
     * The whole request is rejected upfront, before any file is processed,
     * if the picked folder looks like the whole vault rather than a single
     * environment folder - see {@link #LIKELY_VAULT_ROOT_NAME}.
     */
    @Transactional
    public MarkdownVaultUploadResponse uploadVault(List<MultipartFile> files, List<String> paths) throws Exception {
        currentUserService.requireAdmin();

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided.");
        }

        if (paths == null || paths.size() != files.size()) {
            throw new IllegalArgumentException("Each file must have a matching relative path.");
        }

        if (files.size() > MAX_VAULT_FILES) {
            throw new IllegalArgumentException(
                    "Too many files in one vault upload. Maximum is " + MAX_VAULT_FILES + ".");
        }

        String firstRootSegment = firstNonBlankSegment(resolveRelativePath(files.get(0), paths.get(0)));

        if (firstRootSegment != null && LIKELY_VAULT_ROOT_NAME.equalsIgnoreCase(firstRootSegment)) {
            throw new IllegalArgumentException(
                    "\"" + firstRootSegment + "\" looks like your whole Obsidian vault, not a single "
                            + "environment folder. Pick a specific folder inside it (e.g. \"Java\") and "
                            + "try again.");
        }

        List<MarkdownUploadResponse> imported = new ArrayList<>();
        List<MarkdownVaultUploadResponse.VaultFileError> errors = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String relativePath = resolveRelativePath(file, paths.get(i));

            try {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                String rawFilename = file.getOriginalFilename();

                if (rawFilename == null || !rawFilename.toLowerCase().endsWith(".md")) {
                    continue;
                }

                String baseFilename = basename(rawFilename);

                if (file.getSize() > MAX_FILE_SIZE) {
                    errors.add(new MarkdownVaultUploadResponse.VaultFileError(
                            relativePath, "File is too large. Maximum size is 200 KB."));
                    continue;
                }

                String content = new String(file.getBytes(), StandardCharsets.UTF_8);

                if (!hasPathHeading(content)) {
                    // A stub file with no "##" topic yet (e.g. an Obsidian
                    // note created for later) has nothing worth importing.
                    // Without this check, MarkdownTopicImporter's "no ##
                    // headings -> save the whole markdown as one topic"
                    // fallback would save the header line we're about to
                    // inject as if it were the topic's own content.
                    if (!hasTopicHeading(content)) {
                        continue;
                    }

                    content = synthesizeHeaderFromPath(relativePath) + "\n\n" + content;
                }

                imported.add(importMarkdown(content, sanitizeFilename(baseFilename)));

            } catch (IllegalArgumentException e) {
                errors.add(new MarkdownVaultUploadResponse.VaultFileError(relativePath, e.getMessage()));
            }
        }

        return new MarkdownVaultUploadResponse(imported, errors);
    }

    /**
     * Builds a "# Environment / Category / Subcategory / FileTitle" line
     * from a file's path relative to the picked folder. The picked folder
     * itself becomes Environment - vault uploads are expected to scope one
     * environment at a time (e.g. picking "Java", not the whole vault), so
     * its name is meaningful rather than a throwaway container to discard.
     * The next two path segments become Category/Subcategory; anything
     * deeper is ignored. Segments beyond what the path has are left blank,
     * which {@link MarkdownTopicImporter#parseMarkdown} already treats the
     * same as a missing part in a short in-file header (e.g.
     * "# Programming") - falling back to its usual defaults.
     */
    private String synthesizeHeaderFromPath(String relativePath) {
        List<String> segments = new ArrayList<>();

        for (String segment : relativePath.replace('\\', '/').split("/")) {
            if (!segment.isBlank()) {
                segments.add(segment);
            }
        }

        if (segments.isEmpty()) {
            return "# / / / ";
        }

        String fileTitle = stripExtension(segments.remove(segments.size() - 1));

        String environment = segments.size() > 0 ? segments.get(0) : "";
        String category = segments.size() > 1 ? segments.get(1) : "";
        String subcategory = segments.size() > 2 ? segments.get(2) : "";

        return "# %s / %s / %s / %s".formatted(environment, category, subcategory, fileTitle);
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private String basename(String path) {
        String normalized = path.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        return slash >= 0 ? normalized.substring(slash + 1) : normalized;
    }

    /**
     * Some browsers (Chrome confirmed) send the file's full path relative
     * to the picked folder as the multipart filename itself for a
     * directory/webkitdirectory input, which has proven more reliable
     * than the client-supplied {@code paths} entry (built from {@code
     * File.webkitRelativePath}, which doesn't always survive the round
     * trip the same way) - prefer it whenever it actually looks like a
     * path.
     */
    private String resolveRelativePath(MultipartFile file, String fallbackPath) {
        String rawFilename = file != null ? file.getOriginalFilename() : null;

        if (rawFilename != null && (rawFilename.contains("/") || rawFilename.contains("\\"))) {
            return rawFilename;
        }

        return fallbackPath;
    }

    private String firstNonBlankSegment(String relativePath) {
        if (relativePath == null) {
            return null;
        }

        for (String segment : relativePath.replace('\\', '/').split("/")) {
            if (!segment.isBlank()) {
                return segment;
            }
        }

        return null;
    }

    /**
     * Imports starter-pack content, carrying over the template file's
     * display order verbatim (rather than auto-assigning one, as manual
     * uploads do).
     */
    @Transactional
    public MarkdownUploadResponse importMarkdown(String content, Integer displayOrder) throws Exception {
        return importMarkdown(content, displayOrder, null);
    }

    /**
     * Same as {@link #importMarkdown(String, Integer)}, but also copies
     * {@code templateFile}'s optimized/aiGenerated/originalContent onto the
     * created/updated file - used when cloning a starter-pack template file,
     * so a user's copy carries the same AI provenance as the template it
     * came from instead of always starting blank.
     */
    @Transactional
    public MarkdownUploadResponse importMarkdown(
            String content, Integer displayOrder, MarkdownFile templateFile
    ) throws Exception {
        return importMarkdown(content, null, false, displayOrder, null, null, templateFile);
    }

    @Transactional
    public MarkdownUploadResponse createFromContent(String content) throws Exception {
        return createFromContent(content, null, null);
    }

    @Transactional
    public MarkdownUploadResponse createFromContent(
            String content,
            AppliedOptimization appliedOptimization,
            String originalContentSnapshot
    ) throws Exception {
        return importMarkdown(content, null, true, null, appliedOptimization, originalContentSnapshot);
    }

    public MarkdownUploadResponse getById(Long markdownFileId) {
        Long userId = currentUserService.currentUserId();

        MarkdownFile markdownFile = markdownFileRepository.findByIdAndUserId(markdownFileId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Markdown file not found: " + markdownFileId));

        return toResponse(markdownFile);
    }

    @Transactional
    public MarkdownUploadResponse update(Long markdownFileId, String content) throws Exception {
        return update(markdownFileId, content, null, null);
    }

    /**
     * @param appliedOptimization set only when this save follows an accepted
     *                             Optimize preview, unchanged since; null for
     *                             a plain manual edit.
     * @param originalContentSnapshot the pre-optimize content, used only
     *                                when {@code appliedOptimization} is
     *                                {@code STRUCTURED} and only the first
     *                                time this file is optimized - see
     *                                {@link MarkdownFile#markOptimized}.
     */
    @Transactional
    public MarkdownUploadResponse update(
            Long markdownFileId,
            String content,
            AppliedOptimization appliedOptimization,
            String originalContentSnapshot
    ) throws Exception {
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

        MarkdownTopicImporter.ParsedTopic firstTopic = parsedTopics.get(0);

        markdownFile.updateContent(content, contentHash);
        markdownFile.updateMetadata(
                firstTopic.environment(),
                firstTopic.category(),
                firstTopic.subcategory(),
                firstTopic.fileTitle()
        );
        applyOptimizationMetadata(markdownFile, appliedOptimization, originalContentSnapshot);
        markdownFileRepository.save(markdownFile);

        syncTopicsFromMarkdown(markdownFile, parsedTopics, userId, user);

        return toResponse(markdownFile);
    }

    /**
     * Admin-only. Updates the stored original snapshot directly, independent
     * of `content` - backs the original's own edit form.
     *
     * Also doubles as "Add Original": a file that has never been optimized
     * gets one added retroactively here rather than only ever via the AI
     * Optimize path - {@code optimized} means "has an original", not
     * specifically "the AI wrote it", so the two are free to diverge (e.g. a
     * more detailed original than the review-optimized current content).
     */
    @Transactional
    public MarkdownUploadResponse updateOriginal(Long markdownFileId, String content) {
        currentUserService.requireAdmin();

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Original markdown content is empty.");
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Original markdown content is too large.");
        }

        Long userId = currentUserService.currentUserId();

        MarkdownFile markdownFile = markdownFileRepository.findByIdAndUserId(markdownFileId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Markdown file not found: " + markdownFileId));

        if (markdownFile.isOptimized()) {
            markdownFile.updateOriginalContent(content);
        } else {
            markdownFile.markOptimized(content);
        }

        markdownFileRepository.save(markdownFile);

        return toResponse(markdownFile);
    }

    /**
     * Admin-only. Removes the stored original entirely, clearing {@code
     * optimized} - the inverse of the first-time {@link #updateOriginal}
     * capture. {@code content} (the current, optimized-for-review text) is
     * untouched.
     */
    @Transactional
    public MarkdownUploadResponse deleteOriginal(Long markdownFileId) {
        currentUserService.requireAdmin();

        Long userId = currentUserService.currentUserId();

        MarkdownFile markdownFile = markdownFileRepository.findByIdAndUserId(markdownFileId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Markdown file not found: " + markdownFileId));

        markdownFile.clearOriginal();
        markdownFileRepository.save(markdownFile);

        return toResponse(markdownFile);
    }

    /**
     * Admin-only. Returns AI-optimized markdown text without persisting
     * anything - the caller previews it and submits separately through
     * {@link #update} or {@link #createFromContent} if they want to keep it.
     *
     * If {@code content} already has a "#" header, the header/topic titles
     * were a deliberate choice and are never sent to the AI to rewrite -
     * only topic bodies are optimized, and the response is validated to
     * still contain exactly those titles before anything from it is used.
     * Otherwise (raw/unstructured notes) the AI generates the full
     * structure, grounded against {@link AllowedEnvironmentRepository}.
     *
     * The returned {@link OptimizeResult#structured()} tells the caller
     * which {@link AppliedOptimization} to send back on a later
     * create/update call, if the preview is accepted.
     */
    public OptimizeResult optimize(String content) {
        currentUserService.requireAdmin();

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Markdown content is empty.");
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Markdown content is too large.");
        }

        List<MarkdownTopicImporter.ParsedTopic> parsedTopics = markdownTopicImporter.parseMarkdown(content);

        if (parsedTopics.isEmpty()) {
            throw new IllegalArgumentException("Markdown content does not contain any topics.");
        }

        boolean structured = hasPathHeading(content);

        String optimizedContent = structured
                ? optimizeStructuredContent(parsedTopics)
                : optimizeUnstructuredContent(content);

        return new OptimizeResult(optimizedContent, structured);
    }

    private String optimizeStructuredContent(List<MarkdownTopicImporter.ParsedTopic> parsedTopics) {
        List<TopicToOptimize> topics = parsedTopics.stream()
                .map(topic -> new TopicToOptimize(topic.title(), topic.content()))
                .toList();

        String aiResponse = openAiService.optimizeTopicBodies(topics);

        List<MarkdownTopicImporter.ParsedTopic> rewrittenTopics =
                markdownTopicImporter.parseMarkdown(aiResponse);

        Map<String, String> rewrittenBodyByTitle = new HashMap<>();

        for (MarkdownTopicImporter.ParsedTopic rewritten : rewrittenTopics) {
            rewrittenBodyByTitle.put(rewritten.title(), rewritten.content());
        }

        MarkdownTopicImporter.ParsedTopic firstTopic = parsedTopics.get(0);

        StringBuilder result = new StringBuilder();
        result.append("# %s / %s / %s / %s\n\n".formatted(
                firstTopic.environment(),
                firstTopic.category(),
                firstTopic.subcategory(),
                firstTopic.fileTitle()
        ));

        for (MarkdownTopicImporter.ParsedTopic original : parsedTopics) {
            String rewrittenBody = rewrittenBodyByTitle.get(original.title());

            if (rewrittenBody == null) {
                throw new IllegalStateException(
                        "AI response did not preserve the existing topic titles. Please try again.");
            }

            result.append("## %s\n%s\n\n".formatted(original.title(), rewrittenBody));
        }

        return result.toString().trim();
    }

    private String optimizeUnstructuredContent(String content) {
        List<EnvironmentOption> allowedEnvironments = allowedEnvironmentRepository
                .findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(environment -> new EnvironmentOption(environment.getName(), environment.getDescription()))
                .toList();

        String aiResponse = openAiService.generateStructuredMarkdown(content, allowedEnvironments);

        List<MarkdownTopicImporter.ParsedTopic> generatedTopics =
                markdownTopicImporter.parseMarkdown(aiResponse);

        if (generatedTopics.isEmpty() || generatedTopics.size() > MAX_TOPICS_PER_FILE) {
            throw new IllegalStateException(
                    "AI did not return a valid markdown structure. Please try again.");
        }

        if (!hasPathHeading(aiResponse)) {
            throw new IllegalStateException(
                    "AI response is missing the required header line. Please try again.");
        }

        return aiResponse.trim();
    }

    private boolean hasPathHeading(String content) {
        return hasHeadingMatching(content, "^#\\s+.+");
    }

    private boolean hasTopicHeading(String content) {
        return hasHeadingMatching(content, "^##\\s+.+");
    }

    // Mirrors MarkdownTopicImporter's own fence tracking - a "#"/"##" line
    // inside a fenced code block (e.g. a Python/Bash/YAML comment) must not
    // count as a real heading here either, or content whose only code
    // examples contain such comments gets misread as "already has its own
    // header"/"already has real topics", skipping synthesis or the
    // stub-file check in uploadVault for content that never actually had
    // either.
    private boolean hasHeadingMatching(String content, String headingPattern) {
        boolean insideFence = false;

        for (String line : content.split("\\R")) {
            boolean isFenceDelimiter = line.matches("^\\s*(`{3,}|~{3,}).*");

            if (isFenceDelimiter) {
                insideFence = !insideFence;
                continue;
            }

            if (!insideFence && line.matches(headingPattern)) {
                return true;
            }
        }

        return false;
    }

    private MarkdownUploadResponse importMarkdown(String content, String filename) throws Exception {
        return importMarkdown(content, filename, true, null, null, null, null);
    }

    private MarkdownUploadResponse importMarkdown(
            String content,
            String filename,
            boolean autoAssignDisplayOrder,
            Integer explicitDisplayOrder
    ) throws Exception {
        return importMarkdown(content, filename, autoAssignDisplayOrder, explicitDisplayOrder, null, null, null);
    }

    private MarkdownUploadResponse importMarkdown(
            String content,
            String filename,
            boolean autoAssignDisplayOrder,
            Integer explicitDisplayOrder,
            AppliedOptimization appliedOptimization,
            String originalContentSnapshot
    ) throws Exception {
        return importMarkdown(
                content, filename, autoAssignDisplayOrder, explicitDisplayOrder,
                appliedOptimization, originalContentSnapshot, null);
    }

    private MarkdownUploadResponse importMarkdown(
            String content,
            String filename,
            boolean autoAssignDisplayOrder,
            Integer explicitDisplayOrder,
            AppliedOptimization appliedOptimization,
            String originalContentSnapshot,
            MarkdownFile cloneOptimizationFrom
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

        boolean skipTopicSync = false;

        if (existingFile.isPresent()) {
            markdownFile = existingFile.get();

            // A re-import (e.g. re-uploading an Obsidian vault) matches this
            // file by path/title alone, not by content - so if it was
            // previously optimized, the incoming raw content must be compared
            // against the pre-optimize originalContent, not against the
            // current (optimized) content, which is expected to differ.
            // Otherwise every re-upload would silently blow away the
            // optimization by resetting content back to the raw source.
            boolean unchangedSinceOptimization = markdownFile.isOptimized()
                    && markdownFile.getOriginalContent() != null
                    && markdownFile.getOriginalContent().equals(content);

            if (unchangedSinceOptimization) {
                skipTopicSync = true;
            } else {
                markdownFile.updateContent(content, contentHash);
            }

            markdownFile.updateMetadata(
                    firstTopic.environment(),
                    firstTopic.category(),
                    firstTopic.subcategory(),
                    firstTopic.fileTitle()
            );

            if (!autoAssignDisplayOrder) {
                markdownFile.updateDisplayOrder(explicitDisplayOrder);
            }

            applyOptimizationMetadata(markdownFile, appliedOptimization, originalContentSnapshot);
            applyClonedOptimizationMetadata(markdownFile, cloneOptimizationFrom);
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

            applyOptimizationMetadata(markdownFile, appliedOptimization, originalContentSnapshot);
            applyClonedOptimizationMetadata(markdownFile, cloneOptimizationFrom);
            markdownFileRepository.save(markdownFile);
        }

        if (!skipTopicSync) {
            syncTopicsFromMarkdown(markdownFile, parsedTopics, userId, user);
        }

        return toResponse(markdownFile);
    }

    // update()/createFromContent() aren't otherwise admin-gated (any owner
    // can edit their own file), so this flag-setting path is guarded
    // separately - without it, a crafted request could fabricate the
    // optimized/aiGenerated provenance without ever calling optimize().
    private void applyOptimizationMetadata(
            MarkdownFile markdownFile,
            AppliedOptimization appliedOptimization,
            String originalContentSnapshot
    ) {
        if (appliedOptimization == null) {
            return;
        }

        currentUserService.requireAdmin();

        if (appliedOptimization == AppliedOptimization.STRUCTURED) {
            markdownFile.markOptimized(originalContentSnapshot);
        } else {
            markdownFile.markAiGenerated();
        }
    }

    // Used only when cloning a starter-pack template file (see
    // StarterPackService) - copies its optimized/aiGenerated/originalContent
    // as-is, so a user's copy carries the same AI provenance as the template
    // it came from instead of always starting blank. No admin check here:
    // this copies existing, already-trusted server-side data, not
    // user-submitted input.
    private void applyClonedOptimizationMetadata(MarkdownFile markdownFile, MarkdownFile cloneFrom) {
        if (cloneFrom == null) {
            return;
        }

        if (cloneFrom.isOptimized()) {
            markdownFile.markOptimized(cloneFrom.getOriginalContent());
        }

        if (cloneFrom.isAiGenerated()) {
            markdownFile.markAiGenerated();
        }
    }

    private MarkdownUploadResponse toResponse(MarkdownFile markdownFile) {
        return new MarkdownUploadResponse(
                markdownFile.getId(),
                markdownFile.getFilename(),
                markdownFile.getContent(),
                markdownFile.getOriginalContent(),
                markdownFile.isOptimized(),
                markdownFile.isAiGenerated()
        );
    }

    @Transactional
    public long countReviews(Long markdownFileId) {
        Long userId = currentUserService.currentUserId();

        markdownFileRepository.findByIdAndUserId(markdownFileId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Markdown file not found: " + markdownFileId));

        return reviewRepository.countByTopicMarkdownFileIdAndUserId(markdownFileId, userId);
    }

    // Deduped to the most recent attempt per (topic, question) - a "Try Again"
    // resubmission of the same challenge shouldn't show up twice, matching
    // the distinct-question semantics of the dashboard's codeChallengeCount.
    @Transactional
    public List<CodeChallengeResponse> getCodeChallenges(Long markdownFileId) {
        Long userId = currentUserService.currentUserId();

        markdownFileRepository.findByIdAndUserId(markdownFileId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Markdown file not found: " + markdownFileId));

        List<Review> reviews = reviewRepository.findByTopicMarkdownFileIdAndUserIdAndRetryTypeOrderByReviewedAtDesc(
                markdownFileId, userId, RetryType.CODE_CHALLENGE);

        Map<String, Review> mostRecentByTopicAndQuestion = new LinkedHashMap<>();
        for (Review review : reviews) {
            mostRecentByTopicAndQuestion.putIfAbsent(review.getTopic().getId() + "|" + review.getQuestion(), review);
        }

        return mostRecentByTopicAndQuestion.values().stream()
                .sorted(Comparator.comparing(Review::getReviewedAt))
                .map(review -> new CodeChallengeResponse(
                        review.getTopic().getId(),
                        review.getTopic().getTitle(),
                        review.getQuestion(),
                        review.getUserAnswer(),
                        review.getFeedback(),
                        review.getScore(),
                        review.getReviewedAt()))
                .toList();
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
package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import dev.recallforge.domain.AllowedEnvironment;
import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.Review;
import dev.recallforge.domain.RetryType;
import dev.recallforge.domain.Topic;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.dto.CodeChallengeResponse;
import dev.recallforge.dto.MarkdownUploadResponse;
import dev.recallforge.exception.MarkdownFileHasReviewsException;
import dev.recallforge.repository.AllowedEnvironmentRepository;
import dev.recallforge.repository.MarkdownFileRepository;
import dev.recallforge.repository.ReviewRepository;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class MarkdownFileServiceTest {

    @Mock
    private MarkdownFileRepository markdownFileRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MarkdownTopicImporter markdownTopicImporter;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private OpenAiService openAiService;

    @Mock
    private AllowedEnvironmentRepository allowedEnvironmentRepository;

    private MarkdownFileService markdownFileService;

    private void setUp() {
        markdownFileService = new MarkdownFileService(
            markdownFileRepository, topicRepository, reviewRepository, markdownTopicImporter,
            currentUserService, openAiService, allowedEnvironmentRepository
        );
    }

    private static String sha256(String content) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(content.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Test
    void upload_nullFile_throwsIllegalArgumentException() {
        setUp();
        assertThatThrownBy(() -> markdownFileService.upload(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File is empty");
    }

    @Test
    void upload_emptyFile_throwsIllegalArgumentException() {
        setUp();
        MockMultipartFile file = new MockMultipartFile("file", "notes.md", "text/markdown", new byte[0]);

        assertThatThrownBy(() -> markdownFileService.upload(file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File is empty");
    }

    @Test
    void upload_nonMdExtension_throwsIllegalArgumentException() {
        setUp();
        MockMultipartFile file = new MockMultipartFile(
            "file", "notes.txt", "text/plain", "## Topic\ncontent".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> markdownFileService.upload(file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Only .md files are allowed.");
    }

    @Test
    void upload_fileLargerThan200Kb_throwsIllegalArgumentException() {
        setUp();
        byte[] oversized = new byte[200 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("file", "big.md", "text/markdown", oversized);

        assertThatThrownBy(() -> markdownFileService.upload(file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File is too large. Maximum size is 200 KB.");
    }

    @Test
    void upload_contentLongerThan200000Chars_throwsIllegalArgumentException() {
        setUp();
        byte[] oversizedContent = "a".repeat(200_001).getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "big2.md", "text/markdown", oversizedContent);

        assertThatThrownBy(() -> markdownFileService.upload(file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Markdown content is too large.");
    }

    @Test
    void upload_noParsedTopics_throwsIllegalArgumentException() throws Exception {
        setUp();
        MockMultipartFile file = new MockMultipartFile(
            "file", "notes.md", "text/markdown", "not really markdown".getBytes(StandardCharsets.UTF_8)
        );
        when(markdownTopicImporter.parseMarkdown(any())).thenReturn(List.of());

        assertThatThrownBy(() -> markdownFileService.upload(file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Markdown file does not contain any topics.");
    }

    @Test
    void upload_moreThan100ParsedTopics_throwsIllegalArgumentException() throws Exception {
        setUp();
        MockMultipartFile file = new MockMultipartFile(
            "file", "notes.md", "text/markdown", "content".getBytes(StandardCharsets.UTF_8)
        );
        List<MarkdownTopicImporter.ParsedTopic> tooManyTopics = java.util.stream.IntStream.range(0, 101)
            .mapToObj(i -> new MarkdownTopicImporter.ParsedTopic(
                "General", "Uncategorized", "Uncategorized", "File", "Topic " + i, "Content " + i))
            .toList();
        when(markdownTopicImporter.parseMarkdown(any())).thenReturn(tooManyTopics);

        assertThatThrownBy(() -> markdownFileService.upload(file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Too many topics in one markdown file.");
    }

    @Test
    void upload_newFile_createsMarkdownFileWithCorrectHashAndSanitizedFilename() throws Exception {
        setUp();
        String content = "## Java Basics\nSome content.";
        MockMultipartFile file = new MockMultipartFile(
            "file", "../etc/notes.md", "text/markdown", content.getBytes(StandardCharsets.UTF_8)
        );
        MarkdownTopicImporter.ParsedTopic parsedTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Core", "Java Basics", "Java Basics", "Some content."
        );
        UserAccount user = new UserAccount();

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(currentUserService.currentUser()).thenReturn(user);
        when(markdownTopicImporter.parseMarkdown(content)).thenReturn(List.of(parsedTopic));
        when(markdownFileRepository.findByEnvironmentAndCategoryAndSubcategoryAndTopicGroupAndUserId(
                "Programming", "Java", "Core", "Java Basics", 1L))
            .thenReturn(Optional.empty());
        when(topicRepository.findByTitleAndUserId("Java Basics", 1L)).thenReturn(Optional.empty());
        when(markdownFileRepository.findMaxDisplayOrderByUserIdAndCategoryAndSubcategory(1L, "Java", "Core"))
            .thenReturn(Optional.empty());
        when(markdownFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(topicRepository.findByMarkdownFileAndTitleAndUserId(any(), eq("Java Basics"), eq(1L)))
            .thenReturn(Optional.empty());

        MarkdownUploadResponse response = markdownFileService.upload(file);

        assertThat(response.filename()).isEqualTo("etcnotes.md");
        assertThat(response.content()).isEqualTo(content);

        ArgumentCaptor<MarkdownFile> markdownFileCaptor = ArgumentCaptor.forClass(MarkdownFile.class);
        verify(markdownFileRepository).save(markdownFileCaptor.capture());
        MarkdownFile savedMarkdownFile = markdownFileCaptor.getValue();
        assertThat(savedMarkdownFile.getContentHash()).isEqualTo(sha256(content));
        assertThat(savedMarkdownFile.getEnvironment()).isEqualTo("Programming");
        assertThat(savedMarkdownFile.getCategory()).isEqualTo("Java");
        assertThat(savedMarkdownFile.getSubcategory()).isEqualTo("Core");
        assertThat(savedMarkdownFile.getDisplayOrder()).isEqualTo(1);

        ArgumentCaptor<Topic> topicCaptor = ArgumentCaptor.forClass(Topic.class);
        verify(topicRepository).save(topicCaptor.capture());
        Topic savedTopic = topicCaptor.getValue();
        assertThat(savedTopic.getTitle()).isEqualTo("Java Basics");
        assertThat(savedTopic.getContent()).isEqualTo("Some content.");
        assertThat(savedTopic.getMarkdownFile()).isSameAs(savedMarkdownFile);
    }

    @Test
    void upload_newFile_assignsDisplayOrderAfterExistingMaxInSameCategoryAndSubcategory() throws Exception {
        setUp();
        String content = "## Java Basics\nSome content.";
        MockMultipartFile file = new MockMultipartFile(
            "file", "notes.md", "text/markdown", content.getBytes(StandardCharsets.UTF_8)
        );
        MarkdownTopicImporter.ParsedTopic parsedTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Core", "Java Basics", "Java Basics", "Some content."
        );
        UserAccount user = new UserAccount();

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(currentUserService.currentUser()).thenReturn(user);
        when(markdownTopicImporter.parseMarkdown(content)).thenReturn(List.of(parsedTopic));
        when(markdownFileRepository.findByEnvironmentAndCategoryAndSubcategoryAndTopicGroupAndUserId(
                "Programming", "Java", "Core", "Java Basics", 1L))
            .thenReturn(Optional.empty());
        when(topicRepository.findByTitleAndUserId("Java Basics", 1L)).thenReturn(Optional.empty());
        when(markdownFileRepository.findMaxDisplayOrderByUserIdAndCategoryAndSubcategory(1L, "Java", "Core"))
            .thenReturn(Optional.of(4));
        when(markdownFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(topicRepository.findByMarkdownFileAndTitleAndUserId(any(), eq("Java Basics"), eq(1L)))
            .thenReturn(Optional.empty());

        markdownFileService.upload(file);

        ArgumentCaptor<MarkdownFile> markdownFileCaptor = ArgumentCaptor.forClass(MarkdownFile.class);
        verify(markdownFileRepository).save(markdownFileCaptor.capture());
        assertThat(markdownFileCaptor.getValue().getDisplayOrder()).isEqualTo(5);
    }

    @Test
    void upload_existingMarkdownFile_doesNotRecomputeDisplayOrderOnEdit() throws Exception {
        setUp();
        String newContent = "## Java Basics\nUpdated content.";
        MockMultipartFile file = new MockMultipartFile(
            "file", "notes.md", "text/markdown", newContent.getBytes(StandardCharsets.UTF_8)
        );
        MarkdownTopicImporter.ParsedTopic parsedTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Core", "Java Basics", "Java Basics", "Updated content."
        );
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", "old content", "oldhash", "Programming", "Java", "Core", "Java Basics", user
        );
        existingMarkdownFile.updateDisplayOrder(3);

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(currentUserService.currentUser()).thenReturn(user);
        when(markdownTopicImporter.parseMarkdown(newContent)).thenReturn(List.of(parsedTopic));
        when(markdownFileRepository.findByEnvironmentAndCategoryAndSubcategoryAndTopicGroupAndUserId(
                "Programming", "Java", "Core", "Java Basics", 1L))
            .thenReturn(Optional.of(existingMarkdownFile));
        when(markdownFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(topicRepository.findByMarkdownFileAndTitleAndUserId(
                eq(existingMarkdownFile), eq("Java Basics"), eq(1L)))
            .thenReturn(Optional.empty());

        markdownFileService.upload(file);

        assertThat(existingMarkdownFile.getDisplayOrder()).isEqualTo(3);
        verify(markdownFileRepository, org.mockito.Mockito.never())
            .findMaxDisplayOrderByUserIdAndCategoryAndSubcategory(any(), any(), any());
    }

    @Test
    void upload_existingMarkdownFileFoundByMetadata_updatesInPlaceInsteadOfCreatingNew() throws Exception {
        setUp();
        String newContent = "## Java Basics\nUpdated content.";
        MockMultipartFile file = new MockMultipartFile(
            "file", "notes.md", "text/markdown", newContent.getBytes(StandardCharsets.UTF_8)
        );
        MarkdownTopicImporter.ParsedTopic parsedTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Core", "Java Basics", "Java Basics", "Updated content."
        );
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", "old content", "oldhash", "Programming", "Java", "Core", "Java Basics", user
        );

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(currentUserService.currentUser()).thenReturn(user);
        when(markdownTopicImporter.parseMarkdown(newContent)).thenReturn(List.of(parsedTopic));
        when(markdownFileRepository.findByEnvironmentAndCategoryAndSubcategoryAndTopicGroupAndUserId(
                "Programming", "Java", "Core", "Java Basics", 1L))
            .thenReturn(Optional.of(existingMarkdownFile));
        when(markdownFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(topicRepository.findByMarkdownFileAndTitleAndUserId(
                eq(existingMarkdownFile), eq("Java Basics"), eq(1L)))
            .thenReturn(Optional.empty());

        markdownFileService.upload(file);

        assertThat(existingMarkdownFile.getContent()).isEqualTo(newContent);
        assertThat(existingMarkdownFile.getContentHash()).isEqualTo(sha256(newContent));
        verify(markdownFileRepository).save(existingMarkdownFile);
        verify(topicRepository, org.mockito.Mockito.never())
            .findByTitleAndUserId(any(), any());
    }

    @Test
    void upload_existingTopicForMarkdownFile_updatesInPlaceInsteadOfCreatingNew() throws Exception {
        setUp();
        String content = "## Java Basics\nUpdated content.";
        MockMultipartFile file = new MockMultipartFile(
            "file", "notes.md", "text/markdown", content.getBytes(StandardCharsets.UTF_8)
        );
        MarkdownTopicImporter.ParsedTopic parsedTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Core", "Java Basics", "Java Basics", "Updated content."
        );
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", "old content", "oldhash", "Programming", "Java", "Core", "Java Basics", user
        );
        Topic existingTopic = new Topic(
            "Programming", "Java", "Core", "Java Basics",
            "Java Basics", "old topic content", existingMarkdownFile, user
        );

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(currentUserService.currentUser()).thenReturn(user);
        when(markdownTopicImporter.parseMarkdown(content)).thenReturn(List.of(parsedTopic));
        when(markdownFileRepository.findByEnvironmentAndCategoryAndSubcategoryAndTopicGroupAndUserId(
                "Programming", "Java", "Core", "Java Basics", 1L))
            .thenReturn(Optional.of(existingMarkdownFile));
        when(markdownFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(topicRepository.findByMarkdownFileAndTitleAndUserId(
                eq(existingMarkdownFile), eq("Java Basics"), eq(1L)))
            .thenReturn(Optional.of(existingTopic));

        markdownFileService.upload(file);

        assertThat(existingTopic.getContent()).isEqualTo("Updated content.");
        verify(topicRepository).save(existingTopic);
    }

    @Test
    void upload_topicRenamed_deletesOldTopicAndItsReviews() throws Exception {
        setUp();
        String content = "## New Title\nUpdated content.";
        MockMultipartFile file = new MockMultipartFile(
            "file", "notes.md", "text/markdown", content.getBytes(StandardCharsets.UTF_8)
        );
        MarkdownTopicImporter.ParsedTopic parsedTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Core", "Java Basics", "New Title", "Updated content."
        );
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", "old content", "oldhash", "Programming", "Java", "Core", "Java Basics", user
        );
        Topic oldTopic = new Topic(
            "Programming", "Java", "Core", "Java Basics",
            "Old Title", "old topic content", existingMarkdownFile, user
        );

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(currentUserService.currentUser()).thenReturn(user);
        when(markdownTopicImporter.parseMarkdown(content)).thenReturn(List.of(parsedTopic));
        when(markdownFileRepository.findByEnvironmentAndCategoryAndSubcategoryAndTopicGroupAndUserId(
                "Programming", "Java", "Core", "Java Basics", 1L))
            .thenReturn(Optional.of(existingMarkdownFile));
        when(markdownFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(topicRepository.findByMarkdownFileAndTitleAndUserId(
                eq(existingMarkdownFile), eq("New Title"), eq(1L)))
            .thenReturn(Optional.empty());
        when(topicRepository.findByMarkdownFileAndUserIdAndTitleNotIn(
                eq(existingMarkdownFile), eq(1L), eq(List.of("New Title"))))
            .thenReturn(List.of(oldTopic));

        markdownFileService.upload(file);

        verify(reviewRepository).deleteByTopicIdInAndUserId(any(), eq(1L));
        verify(topicRepository).deleteAll(List.of(oldTopic));
    }

    @Test
    void update_contentLongerThan200000Chars_throwsIllegalArgumentException() {
        setUp();
        String oversizedContent = "a".repeat(200_001);

        assertThatThrownBy(() -> markdownFileService.update(1L, oversizedContent))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Markdown content is too large.");
    }

    @Test
    void update_fileNotFoundOrNotOwned_throwsIllegalArgumentException() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(markdownFileRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> markdownFileService.update(5L, "## Topic\ncontent"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Markdown file not found: 5");
    }

    @Test
    void update_noParsedTopics_throwsIllegalArgumentException() throws Exception {
        setUp();
        String content = "not really markdown";
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", "old content", "oldhash", "Programming", "Java", "Core", "Java Basics", user
        );

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(currentUserService.currentUser()).thenReturn(user);
        when(markdownFileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingMarkdownFile));
        when(markdownTopicImporter.parseMarkdown(content)).thenReturn(List.of());

        assertThatThrownBy(() -> markdownFileService.update(1L, content))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Markdown file does not contain any topics.");
    }

    @Test
    void update_moreThan100ParsedTopics_throwsIllegalArgumentException() throws Exception {
        setUp();
        String content = "content";
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", "old content", "oldhash", "Programming", "Java", "Core", "Java Basics", user
        );
        List<MarkdownTopicImporter.ParsedTopic> tooManyTopics = java.util.stream.IntStream.range(0, 101)
            .mapToObj(i -> new MarkdownTopicImporter.ParsedTopic(
                "General", "Uncategorized", "Uncategorized", "File", "Topic " + i, "Content " + i))
            .toList();

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(currentUserService.currentUser()).thenReturn(user);
        when(markdownFileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingMarkdownFile));
        when(markdownTopicImporter.parseMarkdown(content)).thenReturn(tooManyTopics);

        assertThatThrownBy(() -> markdownFileService.update(1L, content))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Too many topics in one markdown file.");
    }

    @Test
    void update_duplicateContentInDifferentFile_throwsMarkdownAlreadyImportedException() throws Exception {
        setUp();
        String newContent = "## Java Basics\nDuplicate content.";
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", "old content", "oldhash", "Programming", "Java", "Core", "Java Basics", user
        );
        MarkdownFile otherMarkdownFile = new MarkdownFile(
            "other.md", newContent, sha256(newContent), "Programming", "Java", "Core", "Other Topic", user
        );
        MarkdownTopicImporter.ParsedTopic parsedTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Core", "Java Basics", "Java Basics", "Duplicate content."
        );

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(currentUserService.currentUser()).thenReturn(user);
        when(markdownFileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingMarkdownFile));
        when(markdownTopicImporter.parseMarkdown(newContent)).thenReturn(List.of(parsedTopic));
        when(markdownFileRepository.findByContentHashAndUserId(sha256(newContent), 1L)).thenReturn(Optional.of(otherMarkdownFile));

        assertThatThrownBy(() -> markdownFileService.update(1L, newContent))
            .isInstanceOf(dev.recallforge.exception.MarkdownAlreadyImportedException.class)
            .hasMessage("This content already exists in another markdown file.");
    }

    @Test
    void update_happyPath_updatesContentHashAndSyncsExistingTopic() throws Exception {
        setUp();
        String newContent = "## Java Basics\nUpdated content.";
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", "old content", "oldhash", "Programming", "Java", "Core", "Java Basics", user
        );
        MarkdownTopicImporter.ParsedTopic parsedTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Core", "Java Basics", "Java Basics", "Updated content."
        );
        Topic existingTopic = new Topic(
            "Programming", "Java", "Core", "Java Basics",
            "Java Basics", "old topic content", existingMarkdownFile, user
        );

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(currentUserService.currentUser()).thenReturn(user);
        when(markdownFileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingMarkdownFile));
        when(markdownTopicImporter.parseMarkdown(newContent)).thenReturn(List.of(parsedTopic));
        when(markdownFileRepository.findByContentHashAndUserId(sha256(newContent), 1L)).thenReturn(Optional.empty());
        when(markdownFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(topicRepository.findByMarkdownFileAndTitleAndUserId(
                eq(existingMarkdownFile), eq("Java Basics"), eq(1L)))
            .thenReturn(Optional.of(existingTopic));

        MarkdownUploadResponse response = markdownFileService.update(1L, newContent);

        assertThat(response.markdownFileId()).isEqualTo(existingMarkdownFile.getId());
        assertThat(response.content()).isEqualTo(newContent);
        assertThat(existingMarkdownFile.getContent()).isEqualTo(newContent);
        assertThat(existingMarkdownFile.getContentHash()).isEqualTo(sha256(newContent));
        verify(markdownFileRepository).save(existingMarkdownFile);
        assertThat(existingTopic.getContent()).isEqualTo("Updated content.");
        verify(topicRepository).save(existingTopic);
    }

    @Test
    void update_headerChanged_syncsMarkdownFileMetadata() throws Exception {
        setUp();
        String newContent = "# Programming / Java / Collections / Java Basics\n## Java Basics\nUpdated content.";
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", "old content", "oldhash", "Programming", "Java", "Core", "Java Basics", user
        );
        MarkdownTopicImporter.ParsedTopic parsedTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Collections", "Java Basics", "Java Basics", "Updated content."
        );
        Topic existingTopic = new Topic(
            "Programming", "Java", "Core", "Java Basics",
            "Java Basics", "old topic content", existingMarkdownFile, user
        );

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(currentUserService.currentUser()).thenReturn(user);
        when(markdownFileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingMarkdownFile));
        when(markdownTopicImporter.parseMarkdown(newContent)).thenReturn(List.of(parsedTopic));
        when(markdownFileRepository.findByContentHashAndUserId(sha256(newContent), 1L)).thenReturn(Optional.empty());
        when(markdownFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(topicRepository.findByMarkdownFileAndTitleAndUserId(
                eq(existingMarkdownFile), eq("Java Basics"), eq(1L)))
            .thenReturn(Optional.of(existingTopic));

        markdownFileService.update(1L, newContent);

        assertThat(existingMarkdownFile.getEnvironment()).isEqualTo("Programming");
        assertThat(existingMarkdownFile.getCategory()).isEqualTo("Java");
        assertThat(existingMarkdownFile.getSubcategory()).isEqualTo("Collections");
        assertThat(existingMarkdownFile.getTopicGroup()).isEqualTo("Java Basics");
    }

    @Test
    void update_contentUnchanged_skipsDuplicateHashLookup() throws Exception {
        setUp();
        String content = "## Java Basics\nSame content.";
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", content, sha256(content), "Programming", "Java", "Core", "Java Basics", user
        );
        MarkdownTopicImporter.ParsedTopic parsedTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Core", "Java Basics", "Java Basics", "Same content."
        );

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(currentUserService.currentUser()).thenReturn(user);
        when(markdownFileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingMarkdownFile));
        when(markdownTopicImporter.parseMarkdown(content)).thenReturn(List.of(parsedTopic));
        when(markdownFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(topicRepository.findByMarkdownFileAndTitleAndUserId(
                eq(existingMarkdownFile), eq("Java Basics"), eq(1L)))
            .thenReturn(Optional.empty());

        markdownFileService.update(1L, content);

        verify(markdownFileRepository, org.mockito.Mockito.never()).findByContentHashAndUserId(any(), any());
    }

    @Test
    void countReviews_ownedFile_returnsCountFromRepository() {
        setUp();
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", "content", "hash", "Programming", "Java", "Core", "Java Basics", user
        );

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(markdownFileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingMarkdownFile));
        when(reviewRepository.countByTopicMarkdownFileIdAndUserId(1L, 1L)).thenReturn(3L);

        long count = markdownFileService.countReviews(1L);

        assertThat(count).isEqualTo(3L);
    }

    @Test
    void countReviews_fileNotOwnedOrMissing_throwsIllegalArgumentException() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(markdownFileRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> markdownFileService.countReviews(5L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Markdown file not found: 5");
    }

    @Test
    void getCodeChallenges_ownedFile_dedupesRepeatedQuestionsKeepingMostRecentAttempt() {
        setUp();
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", "content", "hash", "Programming", "Java", "Core", "Java Basics", user
        );
        Topic topicA = new Topic(
            "Programming", "Java", "Core", "Java Basics",
            "Arrays", "content", existingMarkdownFile, user
        );
        ReflectionTestUtils.setField(topicA, "id", 10L);
        Topic topicB = new Topic(
            "Programming", "Java", "Core", "Java Basics",
            "Lists", "content", existingMarkdownFile, user
        );
        ReflectionTestUtils.setField(topicB, "id", 11L);

        Review newestAttempt = new Review(
            topicA, user, "Write a method that reverses an array.", "newest answer", 0.9, "Good", 100,
            true, RetryType.CODE_CHALLENGE);
        ReflectionTestUtils.setField(newestAttempt, "reviewedAt", LocalDateTime.of(2026, 8, 10, 10, 0));
        Review olderRetriedAttempt = new Review(
            topicA, user, "Write a method that reverses an array.", "older answer", 0.4, "Try again", 80,
            true, RetryType.CODE_CHALLENGE);
        ReflectionTestUtils.setField(olderRetriedAttempt, "reviewedAt", LocalDateTime.of(2026, 8, 5, 10, 0));
        Review distinctChallenge = new Review(
            topicB, user, "Write a method that sorts a list.", "sort answer", 0.8, "Nice", 90,
            true, RetryType.CODE_CHALLENGE);
        ReflectionTestUtils.setField(distinctChallenge, "reviewedAt", LocalDateTime.of(2026, 8, 12, 10, 0));

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(markdownFileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingMarkdownFile));
        // Simulates the repository's own OrderByReviewedAtDesc: most recent first.
        when(reviewRepository.findByTopicMarkdownFileIdAndUserIdAndRetryTypeOrderByReviewedAtDesc(
                1L, 1L, RetryType.CODE_CHALLENGE))
            .thenReturn(List.of(distinctChallenge, newestAttempt, olderRetriedAttempt));

        List<CodeChallengeResponse> challenges = markdownFileService.getCodeChallenges(1L);

        assertThat(challenges).hasSize(2);
        // Ascending by date: the reverse-array challenge (Aug 10) before the sort-list one (Aug 12).
        assertThat(challenges.get(0).question()).isEqualTo("Write a method that reverses an array.");
        assertThat(challenges.get(0).userAnswer()).isEqualTo("newest answer");
        assertThat(challenges.get(1).question()).isEqualTo("Write a method that sorts a list.");
    }

    @Test
    void getCodeChallenges_fileNotOwnedOrMissing_throwsIllegalArgumentException() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(markdownFileRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> markdownFileService.getCodeChallenges(5L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Markdown file not found: 5");
    }

    @Test
    void delete_noReviewsRemaining_deletesTopicsThenMarkdownFile() {
        setUp();
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", "content", "hash", "Programming", "Java", "Core", "Java Basics", user
        );

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(markdownFileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingMarkdownFile));
        when(reviewRepository.countByTopicMarkdownFileIdAndUserId(1L, 1L)).thenReturn(0L);

        markdownFileService.delete(1L);

        verify(topicRepository).deleteByMarkdownFileIdAndUserId(1L, 1L);
        verify(markdownFileRepository).delete(existingMarkdownFile);
    }

    @Test
    void delete_reviewsStillExist_throwsMarkdownFileHasReviewsExceptionAndDeletesNothing() {
        setUp();
        UserAccount user = new UserAccount();
        MarkdownFile existingMarkdownFile = new MarkdownFile(
            "notes.md", "content", "hash", "Programming", "Java", "Core", "Java Basics", user
        );

        when(currentUserService.currentUserId()).thenReturn(1L);
        when(markdownFileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingMarkdownFile));
        when(reviewRepository.countByTopicMarkdownFileIdAndUserId(1L, 1L)).thenReturn(2L);

        assertThatThrownBy(() -> markdownFileService.delete(1L))
            .isInstanceOf(MarkdownFileHasReviewsException.class)
            .hasMessage("Reset reviews for this markdown file before deleting it.");

        verify(topicRepository, org.mockito.Mockito.never()).deleteByMarkdownFileIdAndUserId(any(), any());
        verify(markdownFileRepository, org.mockito.Mockito.never()).delete(any());
    }

    @Test
    void delete_fileNotOwnedOrMissing_throwsIllegalArgumentException() {
        setUp();
        when(currentUserService.currentUserId()).thenReturn(1L);
        when(markdownFileRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> markdownFileService.delete(5L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Markdown file not found: 5");
    }

    @Test
    void optimize_notAdmin_throwsAccessDeniedExceptionWithoutParsing() {
        setUp();
        doThrow(new AccessDeniedException("Admin access required"))
            .when(currentUserService).requireAdmin();

        assertThatThrownBy(() -> markdownFileService.optimize("## Topic\ncontent"))
            .isInstanceOf(AccessDeniedException.class);

        verify(markdownTopicImporter, never()).parseMarkdown(any());
    }

    @Test
    void optimize_blankContent_throwsIllegalArgumentException() {
        setUp();

        assertThatThrownBy(() -> markdownFileService.optimize("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Markdown content is empty.");
    }

    @Test
    void optimize_noParsedTopics_throwsIllegalArgumentException() {
        setUp();
        String content = "no headings here";
        when(markdownTopicImporter.parseMarkdown(content)).thenReturn(List.of());

        assertThatThrownBy(() -> markdownFileService.optimize(content))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Markdown content does not contain any topics.");
    }

    @Test
    void optimize_structuredContent_preservesOriginalTitlesAndHeader() {
        setUp();
        String content = "# Programming / Java / Core / Java Basics\n"
            + "## Variables\nOld variables content.\n"
            + "## Loops\nOld loops content.";

        MarkdownTopicImporter.ParsedTopic variablesTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Core", "Java Basics", "Variables", "Old variables content."
        );
        MarkdownTopicImporter.ParsedTopic loopsTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Core", "Java Basics", "Loops", "Old loops content."
        );

        when(markdownTopicImporter.parseMarkdown(content)).thenReturn(List.of(variablesTopic, loopsTopic));

        String aiResponse = "## Variables\nNew tighter variables content.\n\n"
            + "## Loops\nNew tighter loops content.";

        when(openAiService.optimizeTopicBodies(any())).thenReturn(aiResponse);

        MarkdownTopicImporter.ParsedTopic rewrittenVariables = new MarkdownTopicImporter.ParsedTopic(
            "General", "Uncategorized", "Uncategorized", "Untitled",
            "Variables", "New tighter variables content."
        );
        MarkdownTopicImporter.ParsedTopic rewrittenLoops = new MarkdownTopicImporter.ParsedTopic(
            "General", "Uncategorized", "Uncategorized", "Untitled",
            "Loops", "New tighter loops content."
        );

        when(markdownTopicImporter.parseMarkdown(aiResponse))
            .thenReturn(List.of(rewrittenVariables, rewrittenLoops));

        String result = markdownFileService.optimize(content);

        assertThat(result).startsWith("# Programming / Java / Core / Java Basics");
        assertThat(result).contains("## Variables\nNew tighter variables content.");
        assertThat(result).contains("## Loops\nNew tighter loops content.");
    }

    @Test
    void optimize_structuredContent_aiDropsATitle_throwsIllegalStateException() {
        setUp();
        String content = "# Programming / Java / Core / Java Basics\n"
            + "## Variables\nOld content.\n"
            + "## Loops\nOld loops content.";

        MarkdownTopicImporter.ParsedTopic variablesTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Core", "Java Basics", "Variables", "Old content."
        );
        MarkdownTopicImporter.ParsedTopic loopsTopic = new MarkdownTopicImporter.ParsedTopic(
            "Programming", "Java", "Core", "Java Basics", "Loops", "Old loops content."
        );

        when(markdownTopicImporter.parseMarkdown(content)).thenReturn(List.of(variablesTopic, loopsTopic));

        String aiResponse = "## Variable Basics\nRenamed title, not allowed.";

        when(openAiService.optimizeTopicBodies(any())).thenReturn(aiResponse);

        MarkdownTopicImporter.ParsedTopic renamedTopic = new MarkdownTopicImporter.ParsedTopic(
            "General", "Uncategorized", "Uncategorized", "Untitled",
            "Variable Basics", "Renamed title, not allowed."
        );

        when(markdownTopicImporter.parseMarkdown(aiResponse)).thenReturn(List.of(renamedTopic));

        assertThatThrownBy(() -> markdownFileService.optimize(content))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("AI response did not preserve the existing topic titles. Please try again.");
    }

    @Test
    void optimize_unstructuredContent_returnsAiGeneratedMarkdown() {
        setUp();
        String content = "raw notes about java streams, no headings";

        MarkdownTopicImporter.ParsedTopic fallbackTopic = new MarkdownTopicImporter.ParsedTopic(
            "General", "Uncategorized", "Uncategorized", "Untitled", "Untitled", content
        );

        when(markdownTopicImporter.parseMarkdown(content)).thenReturn(List.of(fallbackTopic));

        AllowedEnvironment jvmEnvironment = new AllowedEnvironment("JVM", "Java, Kotlin, Scala, Spring", 1);
        when(allowedEnvironmentRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(List.of(jvmEnvironment));

        String aiResponse = "# JVM / Java / Streams / Streams Basics\n"
            + "## Stream Creation\nContent about streams.";

        when(openAiService.generateStructuredMarkdown(eq(content), any())).thenReturn(aiResponse);

        MarkdownTopicImporter.ParsedTopic generatedTopic = new MarkdownTopicImporter.ParsedTopic(
            "JVM", "Java", "Streams", "Streams Basics", "Stream Creation", "Content about streams."
        );

        when(markdownTopicImporter.parseMarkdown(aiResponse)).thenReturn(List.of(generatedTopic));

        String result = markdownFileService.optimize(content);

        assertThat(result).isEqualTo(aiResponse);
    }

    @Test
    void optimize_unstructuredContent_aiResponseMissingHeader_throwsIllegalStateException() {
        setUp();
        String content = "raw notes, no headings";

        MarkdownTopicImporter.ParsedTopic fallbackTopic = new MarkdownTopicImporter.ParsedTopic(
            "General", "Uncategorized", "Uncategorized", "Untitled", "Untitled", content
        );

        when(markdownTopicImporter.parseMarkdown(content)).thenReturn(List.of(fallbackTopic));
        when(allowedEnvironmentRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(List.of());

        String aiResponse = "## Just a topic\nNo header line here.";

        when(openAiService.generateStructuredMarkdown(eq(content), any())).thenReturn(aiResponse);

        MarkdownTopicImporter.ParsedTopic generatedTopic = new MarkdownTopicImporter.ParsedTopic(
            "General", "Uncategorized", "Uncategorized", "Untitled",
            "Just a topic", "No header line here."
        );

        when(markdownTopicImporter.parseMarkdown(aiResponse)).thenReturn(List.of(generatedTopic));

        assertThatThrownBy(() -> markdownFileService.optimize(content))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("AI response is missing the required header line. Please try again.");
    }
}

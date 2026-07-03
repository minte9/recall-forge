package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.Topic;
import dev.recallforge.domain.UserAccount;
import dev.recallforge.dto.MarkdownUploadResponse;
import dev.recallforge.repository.MarkdownFileRepository;
import dev.recallforge.repository.TopicRepository;
import dev.recallforge.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class MarkdownFileServiceTest {

    @Mock
    private MarkdownFileRepository markdownFileRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private MarkdownTopicImporter markdownTopicImporter;

    @Mock
    private CurrentUserService currentUserService;

    private MarkdownFileService markdownFileService;

    private void setUp() {
        markdownFileService = new MarkdownFileService(
            markdownFileRepository, topicRepository, markdownTopicImporter, currentUserService
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
            "file", "notes.txt", "text/plain", "### Topic\ncontent".getBytes(StandardCharsets.UTF_8)
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
        String content = "### Java Basics\nSome content.";
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

        ArgumentCaptor<Topic> topicCaptor = ArgumentCaptor.forClass(Topic.class);
        verify(topicRepository).save(topicCaptor.capture());
        Topic savedTopic = topicCaptor.getValue();
        assertThat(savedTopic.getTitle()).isEqualTo("Java Basics");
        assertThat(savedTopic.getContent()).isEqualTo("Some content.");
        assertThat(savedTopic.getMarkdownFile()).isSameAs(savedMarkdownFile);
    }

    @Test
    void upload_existingMarkdownFileFoundByMetadata_updatesInPlaceInsteadOfCreatingNew() throws Exception {
        setUp();
        String newContent = "### Java Basics\nUpdated content.";
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
        String content = "### Java Basics\nUpdated content.";
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
}

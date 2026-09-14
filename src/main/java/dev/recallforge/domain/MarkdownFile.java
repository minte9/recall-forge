package dev.recallforge.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This will generate:
 * 
 * CREATE TABLE markdown_files (
 *      id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *      filename VARCHAR(255) NOT NULL,
 *      content TEXT NOT NULL,
 *      uploaded_at TIMESTAMP NOT NULL
 */

@Entity
@Table(
    name = "markdown_files",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "content_hash"})
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class MarkdownFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String contentHash;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false)
    private String environment;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String subcategory;

    @Column(name = "topic_group")
    private String topicGroup;

    @Column(name = "display_order")
    private Integer displayOrder;

    // Pre-optimize snapshot of `content`, captured once the first time this
    // file is optimized (see markOptimized) and never touched again by later
    // optimize/submit cycles - only its own dedicated edit form changes it
    // from then on, independently of `content`.
    @Column(name = "original_content", columnDefinition = "TEXT")
    private String originalContent;

    // True once this file has gone through the "rewrite existing structured
    // content" Optimize path at least once (i.e. originalContent has been
    // captured).
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean optimized;

    // True if this file's content was produced by the "raw notes -> full
    // structure" Optimize path - a historical fact that, unlike optimized,
    // has no original snapshot behind it.
    @Column(name = "ai_generated", nullable = false, columnDefinition = "boolean default false")
    private boolean aiGenerated;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    public MarkdownFile(
            String filename,
            String content,
            String contentHash,
            String environment,
            String category,
            String subcategory,
            String topicGroup,
            UserAccount user
    ) {
        this.filename = filename;
        this.content = content;
        this.contentHash = contentHash;
        this.environment = environment;
        this.category = category;
        this.subcategory = subcategory;
        this.topicGroup = topicGroup;
        this.user = user;

        this.uploadedAt = LocalDateTime.now();
    }

    public void updateContent(String content, String contentHash) {
        this.content = content;
        this.contentHash = contentHash;
    }

    public void updateMetadata(
        String environment,
        String category,
        String subcategory,
        String topicGroup
    ) {
        this.environment = environment;
        this.category = category;
        this.subcategory = subcategory;
        this.topicGroup = topicGroup;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
     * Marks this file as optimized. The snapshot is only ever captured the
     * first time this is called for a given file (when {@code
     * originalContent} is still null) - a later optimize/submit cycle on an
     * already-optimized file leaves the stored original untouched.
     */
    public void markOptimized(String originalContentSnapshot) {
        if (this.originalContent == null && originalContentSnapshot != null) {
            this.originalContent = originalContentSnapshot;
        }

        this.optimized = true;
    }

    public void markAiGenerated() {
        this.aiGenerated = true;
    }

    /**
     * Direct edit of the stored original, independent of `content` - backs
     * the original's own edit form.
     */
    public void updateOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    /**
     * Removes the stored original entirely and clears {@code optimized} -
     * the inverse of {@link #markOptimized}. {@code content} (the current,
     * optimized-for-review text) is untouched.
     */
    public void clearOriginal() {
        this.originalContent = null;
        this.optimized = false;
    }
}

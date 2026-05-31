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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "topics")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)  // JPA requieres no-args constructor
public class Topic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)  // title from markdown section
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")  // body text under the title
    private String content;

    @Column(nullable = false)  // Average score from previous reviews(0.0 - 1.0)
    private double memoryScore = 0.5;

    private LocalDateTime nextReviewAt;  // when this topic should appear again
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String environment;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String subcategory;

    @Column(nullable = false)
    private String fileTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "markdown_file_id")
    private MarkdownFile markdownFile;

    public Topic(
        String environment, 
        String category,
        String subcategory,
        String fileTitle,
        String title,
        String content,
        MarkdownFile markdownFile
    ) {
        this.environment = environment;
        this.category = category;
        this.subcategory = subcategory;
        this.fileTitle = fileTitle;

        this.title = title;
        this.content = content;
        this.markdownFile = markdownFile;

        this.memoryScore = 0.5;
        this.nextReviewAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateMemory(double memoryScore, LocalDateTime nextReviewAt) {
        this.memoryScore = memoryScore;
        this.nextReviewAt = nextReviewAt;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateFromMarkdown(String fileTitle, String content, MarkdownFile markdownFile) {
        this.fileTitle = fileTitle;
        this.content = content;
        this.markdownFile = markdownFile;
    }
}

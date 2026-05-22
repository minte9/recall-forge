package dev.recallforge.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "topics")
public class Topic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The title from markdown section 
     * Example: "Agent Loop"
     */
    @Column(nullable = false, unique = true)
    private String title;

    /**
     * The body text under the markdown heading.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Average score from previous reviews.
     * 0.0 means unknown / weak.
     * 1.0 means wel known.
     */
    @Column(nullable = false)
    private double memoryScore = 0.5;

    /**
     * When this topic should appear again.
     */
    private LocalDateTime nextReviewAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Topic() {  // JPA requires an entity no-arg constructor
    }

    public Topic(String title, String content) {
        this.title = title;
        this.content = content;
        this.memoryScore = 0.5;
        this.nextReviewAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public double getMemoryScore() {
        return memoryScore;
    }

    public LocalDateTime getNextReviewAt() {
        return nextReviewAt;
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
}

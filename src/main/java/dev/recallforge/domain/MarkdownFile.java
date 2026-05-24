package dev.recallforge.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "markdown_files")
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

    @Column(nullable = false, unique = true)
    private String contentHash;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    public MarkdownFile(String filename, String content, String contentHash) {
        this.filename = filename;
        this.content = content;
        this.contentHash = contentHash;
        this.uploadedAt = LocalDateTime.now();
    }
}

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
 * An image uploaded alongside an Obsidian vault, referenced from markdown
 * by its filename ({@code ![[name.png]]}). Stored markdown keeps the
 * Obsidian embed syntax unchanged; the frontend resolves it to
 * {@code /api/images/{filename}} at render time, so files imported before
 * their images were uploaded start showing them without being re-imported.
 *
 * Filenames are unique per user, never globally - one user's upload can't
 * replace or show up in another user's content.
 *
 * {@code data} holds the server re-encoded bytes (never the raw upload),
 * so metadata and anything appended to the original file are gone.
 */
@Entity
@Table(
    name = "images",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "filename"})
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ImageAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(nullable = false, length = 64)
    private String sha256;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] data;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    public ImageAsset(String filename, String contentType, String sha256, byte[] data, UserAccount user) {
        this.filename = filename;
        this.user = user;
        replaceData(contentType, sha256, data);
    }

    public void replaceData(String contentType, String sha256, byte[] data) {
        this.contentType = contentType;
        this.sha256 = sha256;
        this.data = data;
        this.sizeBytes = data.length;
        this.uploadedAt = LocalDateTime.now();
    }
}

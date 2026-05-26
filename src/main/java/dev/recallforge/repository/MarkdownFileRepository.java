package dev.recallforge.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import dev.recallforge.domain.MarkdownFile;

public interface MarkdownFileRepository extends JpaRepository<MarkdownFile, Long> {

    Optional<MarkdownFile> findByContentHash(String contentHash);
}

package dev.recallforge.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.recallforge.domain.MarkdownFile;

public interface MarkdownFileRepository extends JpaRepository<MarkdownFile, Long> {

    boolean existsByContentHash(String contentHash);
}

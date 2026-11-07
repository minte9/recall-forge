package dev.recallforge.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.recallforge.domain.ImageAsset;

public interface ImageAssetRepository extends JpaRepository<ImageAsset, Long> {

    Optional<ImageAsset> findByUserIdAndFilename(Long userId, String filename);

    long countByUserId(Long userId);

    // Sums the stored size column rather than loading any image bytes.
    @Query("select coalesce(sum(i.sizeBytes), 0) from ImageAsset i where i.user.id = :userId")
    long sumSizeBytesByUserId(@Param("userId") Long userId);
}

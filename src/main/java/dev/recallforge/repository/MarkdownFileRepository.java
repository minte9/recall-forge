package dev.recallforge.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import dev.recallforge.domain.MarkdownFile;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MarkdownFileRepository extends JpaRepository<MarkdownFile, Long> {

    Optional<MarkdownFile> findByEnvironmentAndCategoryAndSubcategoryAndTopicGroupAndUserId(
        String environment,
        String category,
        String subcategory,
        String topicGroup,
        Long userId
    );

    @Modifying
    @Query("delete from MarkdownFile m where m.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("""
        select m
        from MarkdownFile m
        where m.user.id = :userId
        and m.category = :category
        order by m.subcategory asc, m.topicGroup asc
    """)
    List<MarkdownFile> findByUserIdAndCategory(
        @Param("userId") Long userId,
        @Param("category") String category
    );
}

package dev.recallforge.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.recallforge.domain.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByTitle(String title);

    @Query("""
        select t
        from Topic t
        where t.nextReviewAt <= :now
        order by t.memoryScore asc, t.nextReviewAt asc
    """)
    List<Topic> findDueTopics(@Param("now") LocalDateTime now);


    @Query("""
        select t
        from Topic t
        where 
            t.markdownFile.id = :markdownFileId and 
            t.nextReviewAt <= :now
        order by t.memoryScore asc, t.nextReviewAt asc
    """)
    List<Topic> findDueTopicsByMarkdownFileId(Long markdownFileId, LocalDateTime now);

    @Query("""
        select count(t) 
        from Topic t
        where t.nextReviewAt <= :now
    """)
    long countDue(@Param("now") LocalDateTime now);

    @Query("""
        select t
        from Topic t
        where t.nextReviewAt > :now 
        order by t.nextReviewAt asc
    """)
    List<Topic> findNext(@Param("now") LocalDateTime now, Pageable pageable);

    long countByMarkdownFileIdAndNextReviewAtLessThanEqual(
            Long markdownFileId,
            LocalDateTime now
    );

    Optional<Topic> findFirstByMarkdownFileIdAndNextReviewAtGreaterThanOrderByNextReviewAtAsc(
            Long markdownFileId,
            LocalDateTime now
    );

    Optional<Topic> findByEnvironmentAndCategoryAndSubcategoryAndTitle(
        String environment,
        String category,
        String subcategory,
        String title
    );

}

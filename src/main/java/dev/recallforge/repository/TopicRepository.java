package dev.recallforge.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.recallforge.domain.Topic;
import dev.recallforge.dto.MarkdownSummaryDto;

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

    long countByNextReviewAtLessThanEqual(LocalDateTime dateTime);
    long countByNextReviewAtBetween(LocalDateTime start, LocalDateTime end);
    long count();

    @Query("""
        select
            t.environment as environment,
            t.category as category,
            t.subcategory as subcategory,
            count(t) as dueCount,
            avg(t.memoryScore) as averageMemoryScore
        from Topic t
        where t.nextReviewAt <= :now
        group by t.environment, t.category, t.subcategory
        order by count(t) desc, avg(t.memoryScore) asc
    """)
    List<WeakAreaProjection> findWeakAreas(LocalDateTime now);

    @Query("""
        select 
            concat(t.category, ' ', t.subcategory) as title, 
            avg(t.memoryScore) as averageMemoryScore
        from Topic t
        group by t.category, t.subcategory
        order by avg(t.memoryScore) desc
    """)
    List<KnowledgeAreaProjection> findStrongKnowledgeAreas();


    @Query("""
       select 
            concat(t.category, ' ', t.subcategory) as title, 
            avg(t.memoryScore) as averageMemoryScore
        from Topic t
        group by t.category, t.subcategory
        order by avg(t.memoryScore) asc 
    """)
    List<KnowledgeAreaProjection> findWeakKnowledgeAreas();

    @Query("""
        select new dev.recallforge.dto.MarkdownSummaryDto(
            t.markdownFile.id,
            t.category,
            t.subcategory,
            t.fileTitle,
            count(t.id),
            sum(case when t.nextReviewAt <= :now then 1 else 0 end),
            avg(t.memoryScore)
        )
        from Topic t
        group by t.markdownFile.id, t.category, t.subcategory, t.fileTitle
        order by t.category, t.subcategory, t.markdownFile.id
    """)
    List<MarkdownSummaryDto> findMarkdownSummaries(LocalDateTime now);
}

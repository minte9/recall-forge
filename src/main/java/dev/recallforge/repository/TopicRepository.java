package dev.recallforge.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.Topic;
import dev.recallforge.dto.MarkdownSummaryDto;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    // Optional<Topic> findByTitle(String title);
    // Optional<Topic> findByTitleAndMarkdownFileIsNotNull(String title);
    // Optional<Topic> findByMarkdownFileAndTitle(MarkdownFile markdownFile, String title);

    Optional<Topic> findByTitleAndUserId(String title, Long userId);

    Optional<Topic> findByTitleAndMarkdownFileIsNotNullAndUserId(
        String title,
        Long userId
    );

    Optional<Topic> findByMarkdownFileAndTitleAndUserId(
        MarkdownFile markdownFile,
        String title,
        Long userId
    );

    List<Topic> findByUserId(Long userId);
    Optional<Topic> findByIdAndUserId(Long id, Long userId);

    @Query("""
        select t from Topic t
        where t.nextReviewAt <= :now 
            and t.user.id = :userId
        order by t.memoryScore asc, t.nextReviewAt asc
    """)
    List<Topic> findDueTopics(Long userId, LocalDateTime now);

    @Query("""
        select t
        from Topic t
        where 
            t.markdownFile.id = :markdownFileId 
            and t.markdownFile.user.id = :userId
            and t.nextReviewAt <= :now
        order by t.memoryScore asc, t.nextReviewAt asc
    """)
    List<Topic> findDueTopicsByMarkdownFileId(
        @Param("markdownFileId") Long markdownFileId,
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );

    // @Query("""
    //     select count(t) 
    //     from Topic t
    //     where t.nextReviewAt <= :now
    // """)
    // long countDue(@Param("now") LocalDateTime now);

    @Query("""
        select count(t) from Topic t
        where t.user.id = :userId
        and t.nextReviewAt <= :now
    """)
    long countDueByUser(Long userId, LocalDateTime now);

    @Query("""
        select count(t)
        from Topic t
        where t.markdownFile.user.id = :userId
        and t.nextReviewAt <= :now
    """)
    long countDueNowByUser(
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );

    Optional<Topic> findByEnvironmentAndCategoryAndSubcategoryAndTitle(
        String environment,
        String category,
        String subcategory,
        String title
    );

    @Query("""
        select count(t)
        from Topic t
        where t.markdownFile.user.id = :userId
        and t.nextReviewAt >= :start
        and t.nextReviewAt < :end
    """)
    long countDueBetweenByUser(
        @Param("userId") Long userId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        select count(t)
        from Topic t
        where t.markdownFile.user.id = :userId
    """)
    long countByUser(@Param("userId") Long userId);

    @Query("""
        select
            t.environment as environment,
            t.category as category,
            t.subcategory as subcategory,
            count(t) as dueCount,
            avg(t.memoryScore) as averageMemoryScore
        from Topic t
        where t.nextReviewAt <= :now
            and t.markdownFile.user.id = :userId
        group by t.environment, t.category, t.subcategory
        order by count(t) desc, avg(t.memoryScore) asc
    """)
    List<WeakAreaProjection> findWeakAreas(
        @Param("userId") Long userId, 
        @Param("now") LocalDateTime now);

    @Query("""
        select 
            concat(t.category, ', ', t.subcategory) as title, 
            avg(t.memoryScore) as averageMemoryScore
        from Topic t
        where t.markdownFile.user.id = :userId 
        group by t.category, t.subcategory
        order by avg(t.memoryScore) desc
    """)
    List<KnowledgeAreaProjection> findStrongKnowledgeAreas(
        @Param("userId") Long userId
    );


    @Query("""
       select 
            concat(t.category, ', ', t.subcategory) as title, 
            avg(t.memoryScore) as averageMemoryScore
        from Topic t 
        where t.markdownFile.user.id = :userId 
        group by t.category, t.subcategory
        order by avg(t.memoryScore) asc 
    """)
    List<KnowledgeAreaProjection> findWeakKnowledgeAreas(
        @Param("userId") Long userId
    );

    @Query("""
        select new dev.recallforge.dto.MarkdownSummaryDto(
            t.markdownFile.id,
            t.environment,
            t.category,
            t.subcategory,
            t.fileTitle,
            count(t.id),
            sum(case when t.nextReviewAt <= :now then 1 else 0 end),
            avg(t.memoryScore)
        )
        from Topic t
        where t.user.id = :userId
        group by t.markdownFile.id, t.environment, t.category, t.subcategory, t.fileTitle
        order by t.environment, t.category, t.subcategory, t.markdownFile.id
    """)
    List<MarkdownSummaryDto> findMarkdownSummaries(
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );

    @Query("""
        select count(t)
        from Topic t
        where t.markdownFile.id = :markdownFileId
        and t.user.id = :userId
        and t.nextReviewAt <= :now
    """)
    long countDueByMarkdownFileIdAndUser(
        Long markdownFileId,
        Long userId,
        LocalDateTime now
    );

    @Query("""
        select t
        from Topic t
        where t.user.id = :userId
        and t.nextReviewAt > :now
        order by t.nextReviewAt asc
    """)
    List<Topic> findNextByUser(
        Long userId,
        LocalDateTime now,
        Pageable pageable
    );

    @Query("""
        select t
        from Topic t
        where t.markdownFile.id = :markdownFileId
        and t.user.id = :userId
        and t.nextReviewAt > :now
        order by t.nextReviewAt asc
    """)
    List<Topic> findNextByMarkdownFileIdAndUser(
        Long markdownFileId,
        Long userId,
        LocalDateTime now,
        Pageable pageable
    );
}

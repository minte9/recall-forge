package dev.recallforge.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.recallforge.domain.MarkdownFile;
import dev.recallforge.domain.Topic;
import dev.recallforge.dto.MarkdownSummaryDto;

public interface TopicRepository extends JpaRepository<Topic, Long> {

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

    List<Topic> findByMarkdownFileAndUserIdAndTitleNotIn(
        MarkdownFile markdownFile,
        Long userId,
        Collection<String> titles
    );

    List<Topic> findByUserId(Long userId);
    Optional<Topic> findByIdAndUserId(Long id, Long userId);

    @Query("""
        select t from Topic t
        where t.nextReviewAt <= :now
            and t.user.id = :userId
            and lower(t.category) = lower(:category)
        order by t.memoryScore asc, t.nextReviewAt asc
    """)
    List<Topic> findDueTopicsByCategory(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("now") LocalDateTime now
    );

    @Query("""
        select t from Topic t
        where t.user.id = :userId
        and t.markdownFile.id = :markdownFileId
        and (
            t.nextReviewAt is null
            or t.nextReviewAt <= :now
        )
        order by
        case when t.nextReviewAt is null then 0 else 1 end,
        coalesce(t.memoryScore, 0) asc,
        t.nextReviewAt asc
    """)
    List<Topic> findDueTopicsByMarkdownFileId(Long markdownFileId, Long userId, LocalDateTime now);

    @Query("""
        select t from Topic t
        where t.user.id = :userId
        and t.markdownFile.id = :markdownFileId
        order by
        case when t.nextReviewAt is null then 0 else 1 end,
        coalesce(t.memoryScore, 0) asc,
        t.nextReviewAt asc
    """)
    List<Topic> findTopicsByMarkdownFileId(Long markdownFileId, Long userId);

    @Query("""
        select t from Topic t
        where t.user.id = :userId
        and t.markdownFile.id = :markdownFileId
        order by t.id asc
    """)
    List<Topic> findByMarkdownFileIdAndUserIdOrderByIdAsc(
        @Param("markdownFileId") Long markdownFileId,
        @Param("userId") Long userId
    );

    @Query("""
        select count(t) from Topic t
        where t.user.id = :userId
        and lower(t.category) = lower(:category)
        and t.nextReviewAt <= :now
    """)
    long countDueByUser(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("now") LocalDateTime now
    );

    @Query("""
        select count(t) from Topic t
        where t.user.id = :userId
        and lower(t.category) = lower(:category)
        and lower(t.subcategory) = lower(:subcategory)
        and t.nextReviewAt <= :now
    """)
    long countDueByUserAndCategoryAndSubcategory(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("subcategory") String subcategory,
        @Param("now") LocalDateTime now
    );

    @Query("""
        select count(t)
        from Topic t
        where t.markdownFile.user.id = :userId 
        and lower(t.category) = lower(:category) 
        and t.nextReviewAt <= :now         
    """)
    long countDueNowByUser(
        @Param("userId") Long userId,
        @Param("category") String category,
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
        and lower(t.category) = lower(:category)
        and t.nextReviewAt >= :start
        and t.nextReviewAt < :end
    """)
    long countDueBetweenByUser(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        select count(t)
        from Topic t
        where t.markdownFile.user.id = :userId
        and lower(t.category) = lower(:category)
    """)
    long countByUser(
        @Param("userId") Long userId,
        @Param("category") String category
    );

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
            and lower(t.category) = lower(:category)
            and exists (select 1 from Review r where r.topic = t)
        group by t.environment, t.category, t.subcategory
        order by count(t) desc, avg(t.memoryScore) asc
    """)
    List<WeakAreaProjection> findWeakAreas(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("now") LocalDateTime now);

    @Query("""
        select
            t.subcategory as title,
            avg(t.memoryScore) as averageMemoryScore
        from Topic t
        where t.markdownFile.user.id = :userId
        and lower(t.category) = lower(:category)
        group by t.category, t.subcategory
        order by avg(t.memoryScore) desc
    """)
    List<KnowledgeAreaProjection> findStrongKnowledgeAreas(
        @Param("userId") Long userId,
        @Param("category") String category
    );


    @Query("""
       select
            t.subcategory as title,
            avg(t.memoryScore) as averageMemoryScore
        from Topic t
        where t.markdownFile.user.id = :userId
        and lower(t.category) = lower(:category)
        group by t.category, t.subcategory
        order by avg(t.memoryScore) asc
    """)
    List<KnowledgeAreaProjection> findWeakKnowledgeAreas(
        @Param("userId") Long userId,
        @Param("category") String category
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
        and lower(t.category) = lower(:category)
        group by t.markdownFile.id, t.environment, t.category, t.subcategory, t.fileTitle, t.markdownFile.displayOrder
        order by t.environment, t.category, t.subcategory, t.markdownFile.displayOrder asc nulls last, t.markdownFile.id
    """)
    List<MarkdownSummaryDto> findMarkdownSummaries(
        @Param("userId") Long userId,
        @Param("category") String category,
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
        and lower(t.category) = lower(:category)
        and t.nextReviewAt > :now
        order by t.nextReviewAt asc
    """)
    List<Topic> findNextByUser(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("now") LocalDateTime now,
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

    @Query("""
        select count(t)
        from Topic t
        where t.user.id = :userId
        and lower(t.category) = lower(:category)
    """)
    long countByUserIdAndCategory(
        @Param("userId") Long userId,
        @Param("category") String category
    );

    @Query("""
        select count(t)
        from Topic t
        where t.user.id = :userId
        and lower(t.category) = lower(:category)
        and t.memoryScore >= :memoryScore
        and t.nextReviewAt >= :now
    """)
    long countMasteredByUserIdAndCategory(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("memoryScore") double memoryScore,
        @Param("now") LocalDateTime now
    );

    @Query("""
        select distinct
            t.environment as environment,
            t.category as category
        from Topic t
        where t.user.id = :userId
        order by t.environment, t.category
    """)
    List<EnvironmentCategoryProjection> findDistinctEnvironmentCategories(
        @Param("userId") Long userId
    );

    @Modifying
    @Query("delete from Topic t where t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from Topic t where t.markdownFile.id = :markdownFileId and t.user.id = :userId")
    void deleteByMarkdownFileIdAndUserId(
        @Param("markdownFileId") Long markdownFileId,
        @Param("userId") Long userId
    );
}

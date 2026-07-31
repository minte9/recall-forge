package dev.recallforge.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import dev.recallforge.domain.Review;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByTopicIdAndTopicUserIdOrderByReviewedAtDesc(
        Long topicId,
        Long userId
    );

    List<Review> findTop10ByTopicIdAndTopicUserIdOrderByReviewedAtDesc(
        Long topicId,
        Long userId
    );

    List<Review> findTop3ByTopicIdAndTopicUserIdOrderByReviewedAtDesc(
        Long topicId,
        Long userId
    );

    @Modifying
    @Query("delete from Review r where r.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from Review r where r.topic.markdownFile.id = :markdownFileId and r.user.id = :userId")
    void deleteByTopicMarkdownFileIdAndUserId(
        @Param("markdownFileId") Long markdownFileId,
        @Param("userId") Long userId
    );

    @Modifying
    @Query("delete from Review r where r.topic.id in :topicIds and r.user.id = :userId")
    void deleteByTopicIdInAndUserId(
        @Param("topicIds") List<Long> topicIds,
        @Param("userId") Long userId
    );

    long countByTopicMarkdownFileIdAndUserId(Long markdownFileId, Long userId);

    @Query("""
        select distinct r.topic.id
        from Review r
        where r.topic.id in :topicIds
        and r.user.id = :userId
    """)
    List<Long> findReviewedTopicIds(
        @Param("topicIds") List<Long> topicIds,
        @Param("userId") Long userId
    );

    @Query("""
        select count(r)
        from Review r
        where r.user.id = :userId
        and lower(r.topic.category) = lower(:category)
        and r.reviewedAt >= :since
    """)
    long countByUserIdAndCategoryAndReviewedAtAfter(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("since") LocalDateTime since
    );

    @Query("""
        select
            r.topic.subcategory as subcategory,
            count(r) as reviewCount
        from Review r
        where r.user.id = :userId
        and lower(r.topic.category) = lower(:category)
        group by r.topic.subcategory
    """)
    List<SubcategoryReviewCountProjection> countReviewsBySubcategory(
        @Param("userId") Long userId,
        @Param("category") String category
    );

    @Query("""
        select r.reviewedAt
        from Review r
        where r.user.id = :userId
        and lower(r.topic.category) = lower(:category)
        order by r.reviewedAt desc
    """)
    List<LocalDateTime> findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(
        @Param("userId") Long userId,
        @Param("category") String category
    );
}

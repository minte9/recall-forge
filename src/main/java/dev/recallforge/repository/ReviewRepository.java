package dev.recallforge.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import dev.recallforge.domain.Review;
import dev.recallforge.domain.RetryType;

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

    // No Top-N cap: a single topic only ever has a handful of code-challenge
    // attempts, and callers dedupe by question text (repeated "Try Again"
    // resubmissions of the same challenge share one question) rather than
    // relying on row count.
    List<Review> findByTopicIdAndTopicUserIdAndRetryTypeOrderByReviewedAtDesc(
        Long topicId,
        Long userId,
        RetryType retryType
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

    List<Review> findByTopicMarkdownFileIdAndUserIdAndRetryTypeOrderByReviewedAtDesc(
        Long markdownFileId,
        Long userId,
        RetryType retryType
    );

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

    // isRetry = false only - a Try Again / Review Again resubmission is
    // practice, not a fresh day of engagement, so it can't pad the streak.
    @Query("""
        select r.reviewedAt
        from Review r
        where r.user.id = :userId
        and lower(r.topic.category) = lower(:category)
        and r.isRetry = false
        order by r.reviewedAt desc
    """)
    List<LocalDateTime> findReviewedAtByUserIdAndCategoryOrderByReviewedAtDesc(
        @Param("userId") Long userId,
        @Param("category") String category
    );

    @Query("""
        select
            r.topic.markdownFile.id as markdownFileId,
            count(distinct r.question) as codeChallengeCount
        from Review r
        where r.user.id = :userId
        and lower(r.topic.category) = lower(:category)
        and r.retryType = dev.recallforge.domain.RetryType.CODE_CHALLENGE
        group by r.topic.markdownFile.id
    """)
    List<MarkdownFileCodeChallengeCountProjection> countCodeChallengesByMarkdownFile(
        @Param("userId") Long userId,
        @Param("category") String category
    );

    // Per-topic sibling of countCodeChallengesByMarkdownFile, used by the
    // dashboard "Code Challenge" picker to rank topics by how little they've
    // been challenged. Topics with no code-challenge rows simply don't appear
    // here (the caller treats a missing entry as 0).
    @Query("""
        select
            r.topic.id as topicId,
            count(distinct r.question) as codeChallengeCount
        from Review r
        where r.user.id = :userId
        and lower(r.topic.category) = lower(:category)
        and r.retryType = dev.recallforge.domain.RetryType.CODE_CHALLENGE
        group by r.topic.id
    """)
    List<TopicCodeChallengeCountProjection> countCodeChallengesByTopic(
        @Param("userId") Long userId,
        @Param("category") String category
    );

    // Fresh reviews and fresh code-challenge attempts (isRetry = false), the
    // two activity kinds that count toward a "goal day" - a Try Again /
    // Review Again resubmission (isRetry = true) never does, for either kind,
    // so retrying an already-graded question can't be farmed for extra goal
    // days. Returned as raw rows rather than pre-aggregated by day so
    // RewardService can apply the two different per-day thresholds (reviews
    // vs. code challenges) in one pass. Scoped to :since (start of the
    // current month) rather than all history, since goal days reset monthly.
    @Query("""
        select
            r.reviewedAt as reviewedAt,
            r.score as score,
            r.retryType as retryType
        from Review r
        where r.user.id = :userId
        and lower(r.topic.category) = lower(:category)
        and r.reviewedAt >= :since
        and r.isRetry = false
    """)
    List<ReviewGoalActivityProjection> findGoalActivityByUserIdAndCategorySince(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("since") LocalDateTime since
    );

    // isRetry = false only - see findGoalActivityByUserIdAndCategorySince
    // above. Without this, resubmitting an already-perfected challenge would
    // keep adding XP indefinitely.
    @Query("""
        select count(r) from Review r
        where r.user.id = :userId
        and lower(r.topic.category) = lower(:category)
        and r.retryType = dev.recallforge.domain.RetryType.CODE_CHALLENGE
        and r.score >= :minScore
        and r.isRetry = false
    """)
    long countPerfectCodeChallengesByUserIdAndCategory(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("minScore") double minScore);
}

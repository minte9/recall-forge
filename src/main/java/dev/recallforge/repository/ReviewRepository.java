package dev.recallforge.repository;

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

    @Modifying
    @Query("delete from Review r where r.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}

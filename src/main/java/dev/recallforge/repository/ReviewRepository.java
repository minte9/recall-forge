package dev.recallforge.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import dev.recallforge.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByTopicIdAndTopicUserIdOrderByReviewedAtDesc(
        Long topicId,
        Long userId
    );
}

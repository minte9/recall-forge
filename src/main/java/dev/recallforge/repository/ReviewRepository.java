package dev.recallforge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import dev.recallforge.domain.Review;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByTopicIdOrderByReviewedAtDesc(Long topicId);
}

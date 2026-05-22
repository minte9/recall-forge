package dev.recallforge.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dev.recallforge.domain.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByTitle(String title);

    /**
     * Find topics that are due for review.
     */
    @Query("""
        SELECT t
        FROM Topic t
        WHERE t.nextReviewAt <= :now
        ORDER BY t.memoryScore ASC
    """)
    List<Topic> findDueTopics(LocalDateTime now);

    /**
     * Fallback query:
     * if no topic is due, select weakest topic anyway.
     */
    @Query("""
        SELECT t 
        FROM Topic t 
        ORDER by t.memoryScore asc
    """)
    List<Topic> findWeakestTopics();
}

package dev.recallforge.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        and t.markdownFile is not null
        order by t.memoryScore ASC
    """)
    List<Topic> findDueTopics(@Param("now") LocalDateTime now);

    @Query("""
        select t 
        from Topic t 
        order by t.memoryScore asc
    """)
    List<Topic> findWeakestTopics();

}

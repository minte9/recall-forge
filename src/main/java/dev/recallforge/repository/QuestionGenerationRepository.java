package dev.recallforge.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.recallforge.domain.QuestionGeneration;

public interface QuestionGenerationRepository extends JpaRepository<QuestionGeneration, Long> {

    long countByUserIdAndGeneratedAtAfter(Long userId, LocalDateTime since);

    @Modifying
    @Query("delete from QuestionGeneration q where q.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}

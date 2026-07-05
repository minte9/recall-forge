package dev.recallforge.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import dev.recallforge.domain.UserReward;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRewardRepository extends JpaRepository<UserReward, Long> {
    Optional<UserReward> findByUserId(Long userId);

    @Modifying
    @Query("delete from UserReward r where r.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}

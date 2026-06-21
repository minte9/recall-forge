package dev.recallforge.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import dev.recallforge.domain.UserReward;

public interface UserRewardRepository extends JpaRepository<UserReward, Long> {
    Optional<UserReward> findByUserId(Long userId);
}

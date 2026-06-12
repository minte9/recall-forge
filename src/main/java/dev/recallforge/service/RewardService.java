package dev.recallforge.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import dev.recallforge.domain.UserReward;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.repository.UserRewardRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RewardService {
    
    private final UserRewardRepository repository;

    public RewardService(UserRewardRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RewardResponse rewardDefaultUser(double score) {
        UserReward reward = repository.findByUserKey("default")
            .orElseGet(() -> repository.save(new UserReward("default")));

        int baseXp = xpForScore(score);
        boolean perfect = score >= 0.95;

        int gainedXp = baseXp + (perfect ? 5 : 0);

        reward.addReviewReward(gainedXp, perfect, LocalDate.now());
        repository.save(reward);

        return toResponse(reward, gainedXp);
    }

    public RewardResponse getDefaultUserReward() {
        UserReward reward = repository.findByUserKey("default")
            .orElseGet(() -> repository.save(new UserReward("default")));

        return toResponse(reward, 0);
    }

    private int xpForScore(double score) {
        if (score < 0.4) return 1;      // Again
        if (score < 0.7) return 3;      // Hard
        if (score < 0.9) return 6;      // Good
        return 10;                      // Easy
    }

    private RewardResponse toResponse(UserReward reward, int gainedXp) {
        int level = reward.getTotalXp() / 100 + 1;
        int xpInLevel = reward.getTotalXp() % 100;

        return new RewardResponse(
            reward.getTotalXp(),
            level,
            titleForLevel(level),
            xpInLevel,
            100,
            reward.getStreakDays(),
            gainedXp
        );
    }

    private String titleForLevel(int level) {
        if (level < 3) return "JVM Initiate";
        if (level < 6) return "JVM Apprentice";
        if (level < 10) return "JVM Adept";
        return "JVM Master";
    }
}

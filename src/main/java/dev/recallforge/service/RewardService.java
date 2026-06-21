package dev.recallforge.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import dev.recallforge.domain.UserReward;
import dev.recallforge.dto.RewardResponse;
import dev.recallforge.repository.UserRewardRepository;
import dev.recallforge.security.CurrentUserService;
import dev.recallforge.domain.UserReward;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RewardService {
    
    private final UserRewardRepository userRewardRepository;
    private final CurrentUserService currentUserService;

    public RewardService(
        UserRewardRepository userRewardRepository,
        CurrentUserService currentUserService
    ) {
        this.userRewardRepository = userRewardRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public RewardResponse rewardDefaultUser(double score) {
        Long userId = currentUserService.currentUserId();

        UserReward reward = userRewardRepository.findByUserId(userId)
            .orElseGet(() -> userRewardRepository.save(new UserReward(userId)));

        int baseXp = xpForScore(score);
        boolean perfect = score >= 0.95;

        int gainedXp = baseXp + (perfect ? 5 : 0);

        reward.addReviewReward(gainedXp, perfect, LocalDate.now());
        userRewardRepository.save(reward);

        return toResponse(reward, gainedXp);
    }

    @Transactional
    public RewardResponse getCurrentUserReward() {
        Long userId = currentUserService.currentUserId();

        UserReward reward = userRewardRepository.findByUserId(userId)
            .orElseGet(() -> userRewardRepository.save(new UserReward(userId)));

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
        return switch (Math.min(level, 10)) {
            case 1 -> "Learner";
            case 2 -> "Student";
            case 3 -> "Apprentice";
            case 4 -> "Practitioner";
            case 5 -> "Specialist";
            case 6 -> "Expert";
            case 7 -> "Scholar";
            case 8 -> "Mentor";
            case 9 -> "Master";
            default -> "Grandmaster";
        };
    }
}

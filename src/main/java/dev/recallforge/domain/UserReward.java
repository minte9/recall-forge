package dev.recallforge.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_reward")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)  // JPA requieres no-args constructor
public class UserReward {
    
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    private int streakDays;
    private int perfectStreak;
    private LocalDate lastReviewDate;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long xp;

    public UserReward(Long userId) {
        this.userId = userId;
    }

    public void addReviewReward(boolean perfect, LocalDate today) {
        if (lastReviewDate == null) {
            streakDays = 1;
        } else if (lastReviewDate.equals(today.minusDays(1))) {
            streakDays++;
        } else if (!lastReviewDate.equals(today)) {
            streakDays = 1;
        }

        perfectStreak = perfect ? perfectStreak + 1 : 0;
        lastReviewDate = today;
    }

    // The stored streakDays only updates on the next review, so it can be stale
    public int currentStreakDays(LocalDate today) {
        if (lastReviewDate == null) {
            return 0;
        }

        boolean streakAlive = lastReviewDate.equals(today) || lastReviewDate.equals(today.minusDays(1));
        return streakAlive ? streakDays : 0;
    }

    public void addXp(long amount) {
        if (amount > 0) this.xp += amount;
    }
}

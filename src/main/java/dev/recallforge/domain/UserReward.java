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
    private long id;

    @Column(nullable = false, unique = true)
    private String userKey = "default";

    private int totalXp;
    private int streakDays;
    private int perfectStreak;
    private LocalDate lastReviewDate;

    public UserReward(String userKey) {
        this.userKey = userKey;
    }

    public void addReviewReward(int xp, boolean perfect, LocalDate today) {
        this.totalXp += xp;

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
}

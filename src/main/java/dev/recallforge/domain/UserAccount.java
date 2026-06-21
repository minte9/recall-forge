package dev.recallforge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

@Entity
@Table(
    name = "user_accounts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_provider_user_id",
            columnNames = {"provider", "provider_user_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)  // JPA requieres no-args constructor
public class UserAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    private String email;
    private String displayName;
    private String avatarUrl;
    private Instant createdAt;
    private Instant lastLoginAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.lastLoginAt = Instant.now();

        if (this.role == null) { 
            this.role = Role.USER; 
        }
    }
}

package dev.recallforge.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)  // JPA requieres no-args constructor
public class Review {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)  // Many reviews can belong to one topic
    private Topic topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userAnswer;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String feedback;

    @Column(nullable = false)
    private int totalTokens;

    @Column(nullable = false)
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PRIVATE;

    // True for a Try Again / Code Challenge practice attempt - lets callers
    // (e.g. the feedback history's average score) tell those apart from a
    // real graded review. Defaults to false so existing rows stay real
    // reviews when this column backfills onto an already-populated table.
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isRetry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private RetryType retryType = RetryType.REVIEW;

    public Review(
        Topic topic,
        UserAccount user,
        String question,
        String userAnswer,
        double score,
        String feedback,
        int totalTokens,
        boolean isRetry,
        RetryType retryType
    ) {
        this.topic = topic;
        this.user = user;
        this.question = question;
        this.userAnswer = userAnswer;
        this.score = score;
        this.feedback = feedback;
        this.totalTokens = totalTokens;
        this.isRetry = isRetry;
        this.retryType = retryType;

        this.reviewedAt = LocalDateTime.now();
    }
}

package dev.recallforge.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many reviews can belong to one topic.
     */
    @ManyToOne(optional = false)
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
    private LocalDateTime reviewedAt;

    protected Review() {
    }
    
    public Review(Topic topic, String question, String userAnswer, double score, String feedback) {
        this.topic = topic;
        this.question = question;
        this.userAnswer = userAnswer;
        this.score = score;
        this.feedback = feedback;
        this.reviewedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Topic getTopic() {
        return topic;
    }

    public String getQuestion() {
        return question;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public double getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
}

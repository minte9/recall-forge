package dev.recallforge.domain;

import java.time.LocalDateTime;
import jakarta.persistence.*;
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
    private LocalDateTime reviewedAt;
    
    public Review(Topic topic, String question, String userAnswer, double score, String feedback) {
        this.topic = topic;
        this.question = question;
        this.userAnswer = userAnswer;
        this.score = score;
        this.feedback = feedback;
        this.reviewedAt = LocalDateTime.now();
    }
}

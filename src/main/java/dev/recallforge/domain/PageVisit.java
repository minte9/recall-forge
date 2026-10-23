package dev.recallforge.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

// One row per anonymous visitor per day on the public landing page (/login).
// visitorHash is a keyed one-way hash of date + IP + user-agent (see PageVisitService);
// no raw IP or user-agent is stored.
@Entity
@Table(
    name = "page_visits",
    uniqueConstraints = @UniqueConstraint(columnNames = {"visit_date", "visitor_hash"})
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)  // JPA requires no-args constructor
public class PageVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "visitor_hash", nullable = false, length = 64)
    private String visitorHash;

    public PageVisit(LocalDate visitDate, String visitorHash) {
        this.visitDate = visitDate;
        this.visitorHash = visitorHash;
    }
}

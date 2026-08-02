package dev.recallforge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The curated, admin-managed list of environment names the AI "Optimize"
 * feature is grounded on when it has to invent a header for unstructured
 * content. Not enforced on manual uploads/edits — {@code environment} stays
 * a free-text column on {@link MarkdownFile}/{@link Topic}; this table only
 * shapes what the AI prefers to propose.
 */
@Entity
@Table(
    name = "allowed_environments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"name"})
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class AllowedEnvironment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public AllowedEnvironment(String name, String description, Integer displayOrder) {
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
    }
}

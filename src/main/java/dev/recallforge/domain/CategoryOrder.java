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

@Entity
@Table(name = "category_order", uniqueConstraints = @UniqueConstraint(columnNames = {"environment", "category"}))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class CategoryOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String environment;

    @Column(nullable = false)
    private String category;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public CategoryOrder(String environment, String category, Integer displayOrder) {
        this.environment = environment;
        this.category = category;
        this.displayOrder = displayOrder;
    }
}

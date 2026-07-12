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
@Table(name = "subcategory_order", uniqueConstraints = @UniqueConstraint(columnNames = {"category", "subcategory"}))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class SubcategoryOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String subcategory;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public SubcategoryOrder(String category, String subcategory, Integer displayOrder) {
        this.category = category;
        this.subcategory = subcategory;
        this.displayOrder = displayOrder;
    }
}

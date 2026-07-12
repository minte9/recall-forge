package dev.recallforge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.recallforge.domain.SubcategoryOrder;

public interface SubcategoryOrderRepository extends JpaRepository<SubcategoryOrder, Long> {

    List<SubcategoryOrder> findByCategoryIgnoreCase(String category);
}

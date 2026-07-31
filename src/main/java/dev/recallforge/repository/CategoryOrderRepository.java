package dev.recallforge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.recallforge.domain.CategoryOrder;

public interface CategoryOrderRepository extends JpaRepository<CategoryOrder, Long> {

    List<CategoryOrder> findByEnvironmentIgnoreCase(String environment);
}

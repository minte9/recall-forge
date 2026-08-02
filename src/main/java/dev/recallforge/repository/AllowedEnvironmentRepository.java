package dev.recallforge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.recallforge.domain.AllowedEnvironment;

public interface AllowedEnvironmentRepository extends JpaRepository<AllowedEnvironment, Long> {

    List<AllowedEnvironment> findAllByOrderByDisplayOrderAsc();
}

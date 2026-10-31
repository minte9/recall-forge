package dev.recallforge.repository;


import dev.recallforge.domain.JobProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobProfileRepository
        extends JpaRepository<JobProfile, Long> {

    // Predefined profiles from template user, in creation (id) order
    List<JobProfile> findByUserIdAndEnabledTrueOrderByIdAsc(
        Long userId
    );

    // Validate a selected template profile
    Optional<JobProfile> findByIdAndUserIdAndEnabledTrue(
        Long id,
        Long userId
    );
}

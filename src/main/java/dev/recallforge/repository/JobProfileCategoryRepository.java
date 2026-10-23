package dev.recallforge.repository;

import dev.recallforge.domain.JobProfileCategory;
import dev.recallforge.domain.JobProfileCategoryId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobProfileCategoryRepository
        extends JpaRepository<JobProfileCategory, JobProfileCategoryId> {

    List<JobProfileCategory> findByIdProfileId(
        Long profileId
    );

    @Query("""
        SELECT j.id.categoryId
        FROM JobProfileCategory j
        WHERE j.id.profileId = :profileId
    """)
    List<Long> findCategoryIdsByProfileId(
        @Param("profileId") Long profileId
    );

    @Query(value = """
        SELECT
            co.id AS id,
            co.category AS category,
            co.environment AS environment
        FROM job_profile_categories jpc
        JOIN category_order co
            ON co.id = jpc.category_id
        WHERE jpc.profile_id = :profileId
        ORDER BY co.environment, co.display_order
        """, nativeQuery = true)
    List<JobProfileCategoryProjection> findCategories(
        @Param("profileId") Long profileId
    );
}
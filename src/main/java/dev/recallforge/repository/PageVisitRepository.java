package dev.recallforge.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.recallforge.domain.PageVisit;

public interface PageVisitRepository extends JpaRepository<PageVisit, Long> {

    boolean existsByVisitDateAndVisitorHash(LocalDate visitDate, String visitorHash);

    @Query("""
        select v.visitDate as visitDate, count(v) as visitors
        from PageVisit v
        where v.visitDate >= :since
        group by v.visitDate
        order by v.visitDate desc
        """)
    List<DailyVisitProjection> countDailyVisitorsSince(@Param("since") LocalDate since);
}

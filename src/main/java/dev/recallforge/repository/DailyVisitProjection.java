package dev.recallforge.repository;

import java.time.LocalDate;

public interface DailyVisitProjection {
    LocalDate getVisitDate();
    long getVisitors();
}

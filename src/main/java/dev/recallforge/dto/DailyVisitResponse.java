package dev.recallforge.dto;

import java.time.LocalDate;

public record DailyVisitResponse(
    LocalDate date,
    long visitors
) {}

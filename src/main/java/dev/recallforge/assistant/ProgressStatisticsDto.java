package dev.recallforge.assistant;

public record ProgressStatisticsDto(
    String category,
    MonthlyStats previousMonth,
    MonthlyStats currentMonth,
    Double averageScoreChange,
    Integer reviewCountChange
) {
    public record MonthlyStats(
        String period,
        long reviewCount,
        Double averageScore,
        Double highScoreRate
    ) {}
}
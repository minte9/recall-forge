package dev.recallforge.assistant;

public interface ReviewStatsProjection {

    Long getReviewCount();

    Double getAverageScore();

    Double getHighScoreRate();
}
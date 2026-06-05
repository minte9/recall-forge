package dev.recallforge.repository;

public interface WeakAreaProjection {
    String getEnvironment();
    String getCategory();
    String getSubcategory();
    long getDueCount();
    double getAverageMemoryScore();
}
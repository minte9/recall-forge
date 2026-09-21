package dev.recallforge.repository;

public interface CategoryDueStatusProjection {
    String getCategory();
    long getReviewedCount();
    long getDueCount();
}

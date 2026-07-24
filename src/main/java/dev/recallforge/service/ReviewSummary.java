package dev.recallforge.service;

public record ReviewSummary(
    String question,
    double score,
    String feedback
) {
}

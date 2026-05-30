package dev.recallforge.dto;

import java.time.LocalDateTime;

public record ReviewQueueResponse(
    long dueCount,
    LocalDateTime nextReviewAt,
    boolean doneForToday
) {    
}

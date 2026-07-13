package dev.recallforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class DailyReviewLimitExceededException extends RuntimeException {
    public DailyReviewLimitExceededException() {
        super("You've hit your daily review limit — nice work today! Come back tomorrow for more.");
    }
}

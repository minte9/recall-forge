package dev.recallforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class DailyOptimizeLimitExceededException extends RuntimeException {
    public DailyOptimizeLimitExceededException() {
        super("You've hit your daily optimize limit. Come back tomorrow for more.");
    }
}

package dev.recallforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class DailyQuestionLimitExceededException extends RuntimeException {
    public DailyQuestionLimitExceededException() {
        super("You've hit your daily question limit for this category. Come back tomorrow for more.");
    }
}

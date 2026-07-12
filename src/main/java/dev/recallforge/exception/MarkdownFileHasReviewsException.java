package dev.recallforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MarkdownFileHasReviewsException extends RuntimeException {

    public MarkdownFileHasReviewsException(String message) {
        super(message);
    }
}

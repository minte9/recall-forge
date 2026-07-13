package dev.recallforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NoDueTopicsException.class)
    public ResponseEntity<String> handleNoDueTopicsException(NoDueTopicsException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ex.getMessage());
    }

    @ExceptionHandler(TemplateUserOperationException.class)
    public ResponseEntity<String> handTemplateUserOperation(TemplateUserOperationException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ex.getMessage());
    }

    @ExceptionHandler(MarkdownAlreadyImportedException.class)
    public ResponseEntity<String> handleMarkdownAlreadyImported(MarkdownAlreadyImportedException ex) {
        return ResponseEntity
            .status(409)
            .body(ex.getMessage());
    }

    @ExceptionHandler(MarkdownFileHasReviewsException.class)
    public ResponseEntity<String> handleMarkdownFileHasReviews(MarkdownFileHasReviewsException ex) {
        return ResponseEntity
            .status(409)
            .body(ex.getMessage());
    }

    @ExceptionHandler(LoginDisabledException.class)
    public ResponseEntity<String> handleLoginDisabled(LoginDisabledException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ex.getMessage());
    }

    @ExceptionHandler(DailyReviewLimitExceededException.class)
    public ResponseEntity<String> handleDailyReviewLimitExceeded(DailyReviewLimitExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ex.getMessage());
    }
}
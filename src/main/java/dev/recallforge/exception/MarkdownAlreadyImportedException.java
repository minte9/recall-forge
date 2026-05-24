package dev.recallforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MarkdownAlreadyImportedException extends RuntimeException {

    public MarkdownAlreadyImportedException(String message) {
        super(message);
    }
}
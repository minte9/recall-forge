package dev.recallforge.controller;

import dev.recallforge.exception.MarkdownAlreadyImportedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MarkdownAlreadyImportedException.class)
    public ResponseEntity<String> handleMarkdownAlreadyImported(
            MarkdownAlreadyImportedException exception
    ) {
        return ResponseEntity
                .status(409)
                .body(exception.getMessage());
    }
}
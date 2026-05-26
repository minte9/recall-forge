package dev.recallforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class ApiExceptionHandler {
 
    @ExceptionHandler(NoDueTopicsException.class)
    public ResponseEntity<String> handleNoDueTopicsException(NoDueTopicsException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ex.getMessage());
    }
}
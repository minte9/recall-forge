package dev.recallforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoDueTopicsException extends RuntimeException {
    public NoDueTopicsException() {
        super("No due topics right now. Try again later or upload a new markdown file.");
    }
    
}

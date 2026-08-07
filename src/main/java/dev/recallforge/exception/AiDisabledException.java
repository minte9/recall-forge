package dev.recallforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class AiDisabledException extends RuntimeException {
    public AiDisabledException() {
        super("AI-powered reviews are currently disabled.");
    }
}

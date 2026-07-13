package dev.recallforge.exception;

public class LoginDisabledException extends RuntimeException {

    public LoginDisabledException() {
        super("Login is temporarily disabled.");
    }
}
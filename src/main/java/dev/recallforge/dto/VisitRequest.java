package dev.recallforge.dto;

// Body of the login-page beacon; referrer is the browser's document.referrer (may be empty).
public record VisitRequest(
    String referrer
) {}

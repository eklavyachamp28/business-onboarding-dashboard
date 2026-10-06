package com.aayusheklavya.onboarding.service;

import org.springframework.http.HttpStatus;

public class OnboardingException extends RuntimeException {
    private final HttpStatus status;

    public OnboardingException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    public static OnboardingException notFound(String what) {
        return new OnboardingException(HttpStatus.NOT_FOUND, what + " not found");
    }

    public static OnboardingException conflict(String message) {
        return new OnboardingException(HttpStatus.CONFLICT, message);
    }

    public static OnboardingException invalid(String message) {
        return new OnboardingException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}

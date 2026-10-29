package com.example.biansimulator.identity.application;

public class LoginBlockedException extends RuntimeException {

    private final long retryAfterSeconds;

    public LoginBlockedException(long retryAfterSeconds) {
        super("Demasiados intentos fallidos");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long retryAfterSeconds() {
        return retryAfterSeconds;
    }
}

package com.example.biansimulator.shared.error;

import java.net.URI;

/** Identificadores estables de tipos de error (RFC 9457). */
public final class ProblemTypes {

    private static final String BASE = "urn:bian-simulator:problem:";

    public static final URI NOT_FOUND = URI.create(BASE + "not-found");
    public static final URI VALIDATION = URI.create(BASE + "validation");
    public static final URI UNAUTHORIZED = URI.create(BASE + "unauthorized");
    public static final URI FORBIDDEN = URI.create(BASE + "forbidden");
    public static final URI INVALID_CREDENTIALS = URI.create(BASE + "invalid-credentials");
    public static final URI LOGIN_BLOCKED = URI.create(BASE + "login-blocked");
    public static final URI INTERNAL = URI.create(BASE + "internal");

    private ProblemTypes() {
    }
}

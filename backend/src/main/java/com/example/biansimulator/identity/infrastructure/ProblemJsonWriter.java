package com.example.biansimulator.identity.infrastructure;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/** Respuestas RFC 9457 escritas desde filtros de seguridad (fuera de Spring MVC). */
final class ProblemJsonWriter {

    private ProblemJsonWriter() {
    }

    static void write(HttpServletResponse response, int status, URI type, String title, String detail)
            throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/problem+json");
        response.getWriter().write("{\"type\":\"" + type + "\",\"title\":\"" + title + "\",\"status\":" + status
                + ",\"detail\":\"" + detail + "\"}");
    }
}

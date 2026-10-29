package com.example.biansimulator.audit.application;

import com.example.biansimulator.audit.domain.AuditOutcome;
import java.util.Map;
import java.util.UUID;

/**
 * Datos de un evento a auditar. {@code details} nunca debe contener credenciales,
 * tokens ni identificadores de sesión.
 */
public record AuditEntry(UUID actorUserId, String actorUsername, String action, String targetType,
        String targetId, AuditOutcome outcome, Map<String, Object> details, String clientIp) {

    public AuditEntry {
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}

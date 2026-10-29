package com.example.biansimulator.audit.application;

import com.example.biansimulator.audit.domain.AuditEvent;
import com.example.biansimulator.audit.infrastructure.AuditEventRepository;
import java.time.Clock;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/** API pública del módulo de auditoría para el resto de módulos. */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository repository;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    public AuditService(AuditEventRepository repository, JsonMapper jsonMapper, Clock clock) {
        this.repository = repository;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    /**
     * Registra el evento en una transacción propia: la auditoría de un intento
     * fallido debe persistir aunque la operación principal se revierta.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditEntry entry) {
        String details = entry.details().isEmpty() ? null : jsonMapper.writeValueAsString(entry.details());
        repository.save(new AuditEvent(UUID.randomUUID(), clock.instant(), entry.actorUserId(),
                entry.actorUsername(), entry.action(), entry.targetType(), entry.targetId(), entry.outcome(),
                details, entry.clientIp()));
        log.info("audit action={} outcome={} actor={}", entry.action(), entry.outcome(), entry.actorUsername());
    }
}

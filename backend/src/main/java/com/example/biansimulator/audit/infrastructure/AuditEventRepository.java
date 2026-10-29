package com.example.biansimulator.audit.infrastructure;

import com.example.biansimulator.audit.domain.AuditEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    List<AuditEvent> findByActorUsernameAndActionOrderByOccurredAtDesc(String actorUsername, String action);
}

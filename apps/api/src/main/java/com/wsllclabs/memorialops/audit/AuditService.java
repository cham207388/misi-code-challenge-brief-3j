package com.wsllclabs.memorialops.audit;

import com.wsllclabs.memorialops.exception.ProblemSupport;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final Clock clock;

    public AuditService(AuditEventRepository auditEventRepository, Clock clock) {
        this.auditEventRepository = auditEventRepository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public AuditEventEntity append(
            UUID actorId,
            String actorRole,
            String action,
            String entityType,
            UUID entityId,
            String beforeJson,
            String afterJson,
            String ip) {
        return auditEventRepository.save(AuditEventEntity.of(
                Instant.now(clock),
                actorId,
                actorRole,
                action,
                entityType,
                entityId,
                beforeJson,
                afterJson,
                ProblemSupport.currentCorrelationId(),
                ip));
    }
}

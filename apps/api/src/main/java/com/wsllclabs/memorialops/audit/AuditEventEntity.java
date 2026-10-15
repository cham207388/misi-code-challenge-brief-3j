package com.wsllclabs.memorialops.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_event")
public class AuditEventEntity {

    @Id
    private UUID id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "actor_role")
    private String actorRole;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_state")
    private String beforeState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_state")
    private String afterState;

    @Column(name = "correlation_id")
    private String correlationId;

    private String ip;

    protected AuditEventEntity() {}

    public static AuditEventEntity of(
            Instant occurredAt,
            UUID actorId,
            String actorRole,
            String action,
            String entityType,
            UUID entityId,
            String beforeState,
            String afterState,
            String correlationId,
            String ip) {
        AuditEventEntity event = new AuditEventEntity();
        event.id = UUID.randomUUID();
        event.occurredAt = occurredAt;
        event.actorId = actorId;
        event.actorRole = actorRole;
        event.action = action;
        event.entityType = entityType;
        event.entityId = entityId;
        event.beforeState = beforeState;
        event.afterState = afterState;
        event.correlationId = correlationId;
        event.ip = ip;
        return event;
    }

    public UUID getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public UUID getEntityId() {
        return entityId;
    }
}

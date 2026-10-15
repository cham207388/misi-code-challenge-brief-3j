package com.wsllclabs.memorialops.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "status_history")
public class StatusHistory {

    @Id
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private ApplicationStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private ApplicationStatus toStatus;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    private String reason;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected StatusHistory() {}

    public static StatusHistory record(
            UUID applicationId,
            ApplicationStatus from,
            ApplicationStatus to,
            UUID actorId,
            String reason,
            Instant occurredAt) {
        StatusHistory history = new StatusHistory();
        history.id = UUID.randomUUID();
        history.applicationId = applicationId;
        history.fromStatus = from;
        history.toStatus = to;
        history.actorId = actorId;
        history.reason = reason;
        history.occurredAt = occurredAt;
        return history;
    }

    public UUID getId() {
        return id;
    }

    public ApplicationStatus getFromStatus() {
        return fromStatus;
    }

    public ApplicationStatus getToStatus() {
        return toStatus;
    }
}

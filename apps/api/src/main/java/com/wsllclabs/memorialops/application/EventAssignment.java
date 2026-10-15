package com.wsllclabs.memorialops.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "event_assignment")
public class EventAssignment {

    @Id
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "attendant_id", nullable = false)
    private UUID attendantId;

    protected EventAssignment() {}

    public UUID getApplicationId() {
        return applicationId;
    }

    public UUID getAttendantId() {
        return attendantId;
    }
}

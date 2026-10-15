package com.wsllclabs.memorialops.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event")
public class EventRecord {

    @Id
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    private String title;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "attendee_count")
    private Integer attendeeCount;

    private String notes;

    @Version
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EventRecord() {}

    public static EventRecord create(UUID applicationId, Instant now) {
        EventRecord event = new EventRecord();
        event.id = UUID.randomUUID();
        event.applicationId = applicationId;
        event.createdAt = now;
        event.updatedAt = now;
        return event;
    }

    public void update(
            String title, Instant startsAt, Instant endsAt, Integer attendeeCount, String notes, Instant now) {
        this.title = title;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.attendeeCount = attendeeCount;
        this.notes = notes;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public String getTitle() {
        return title;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public Integer getAttendeeCount() {
        return attendeeCount;
    }

    public String getNotes() {
        return notes;
    }
}

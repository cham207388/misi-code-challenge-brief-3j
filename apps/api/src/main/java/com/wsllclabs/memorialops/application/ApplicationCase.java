package com.wsllclabs.memorialops.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application")
public class ApplicationCase {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(name = "applicant_id", nullable = false)
    private UUID applicantId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "cemetery_id")
    private UUID cemeteryId;

    @Column(name = "venue_id")
    private UUID venueId;

    @Column(name = "event_category_id")
    private UUID eventCategoryId;

    @Column(name = "wizard_step", nullable = false)
    private short wizardStep;

    @Column(name = "attachment_ready", nullable = false)
    private boolean attachmentReady;

    @Column(name = "reject_stage")
    private String rejectStage;

    @Column(name = "last_reason")
    private String lastReason;

    @Version
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected ApplicationCase() {}

    public static ApplicationCase draft(String reference, UUID applicantId, UUID ownerId, Instant now) {
        ApplicationCase application = new ApplicationCase();
        application.id = UUID.randomUUID();
        application.reference = reference;
        application.status = ApplicationStatus.DRAFT;
        application.applicantId = applicantId;
        application.ownerId = ownerId;
        application.wizardStep = 1;
        application.createdAt = now;
        application.updatedAt = now;
        application.createdBy = ownerId;
        application.updatedBy = ownerId;
        return application;
    }

    public void updateDraft(
            UUID cemeteryId,
            UUID venueId,
            UUID eventCategoryId,
            int wizardStep,
            boolean attachmentReady,
            UUID actorId,
            Instant now) {
        this.cemeteryId = cemeteryId;
        this.venueId = venueId;
        this.eventCategoryId = eventCategoryId;
        this.wizardStep = (short) wizardStep;
        this.attachmentReady = attachmentReady;
        this.updatedBy = actorId;
        this.updatedAt = now;
    }

    public void transitionTo(ApplicationStatus target, String reason, String rejectStage, UUID actorId, Instant now) {
        this.status = target;
        this.lastReason = reason;
        this.rejectStage = rejectStage;
        this.updatedBy = actorId;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public UUID getApplicantId() {
        return applicantId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getCemeteryId() {
        return cemeteryId;
    }

    public UUID getVenueId() {
        return venueId;
    }

    public UUID getEventCategoryId() {
        return eventCategoryId;
    }

    public int getWizardStep() {
        return wizardStep;
    }

    public boolean isAttachmentReady() {
        return attachmentReady;
    }

    public String getRejectStage() {
        return rejectStage;
    }

    public String getLastReason() {
        return lastReason;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}

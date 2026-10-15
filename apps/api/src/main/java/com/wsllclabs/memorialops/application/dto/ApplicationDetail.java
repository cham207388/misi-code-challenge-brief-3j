package com.wsllclabs.memorialops.application.dto;

import com.wsllclabs.memorialops.application.ApplicationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApplicationDetail(
        UUID id,
        String reference,
        ApplicationStatus status,
        ApplicantView applicant,
        EventView event,
        UUID ownerId,
        UUID cemeteryId,
        UUID venueId,
        UUID eventCategoryId,
        int wizardStep,
        boolean attachmentReady,
        String rejectStage,
        String lastReason,
        long version,
        String etag,
        Instant createdAt,
        Instant updatedAt,
        List<String> history) {}

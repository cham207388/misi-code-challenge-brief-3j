package com.wsllclabs.memorialops.application.dto;

import com.wsllclabs.memorialops.application.ApplicationStatus;
import java.time.Instant;
import java.util.UUID;

public record ApplicationSummary(
        UUID id,
        String reference,
        ApplicationStatus status,
        String applicantName,
        UUID ownerId,
        UUID cemeteryId,
        UUID venueId,
        int wizardStep,
        long version,
        String etag,
        Instant createdAt,
        Instant updatedAt) {}

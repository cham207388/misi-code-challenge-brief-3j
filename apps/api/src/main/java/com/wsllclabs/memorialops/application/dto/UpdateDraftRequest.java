package com.wsllclabs.memorialops.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record UpdateDraftRequest(
        @Size(max = 200) String applicantName,
        @Size(max = 255) String applicantEmail,
        @Size(max = 40) String applicantPhone,
        @Size(max = 200) String organization,
        UUID cemeteryId,
        UUID venueId,
        UUID eventCategoryId,
        @Size(max = 200) String eventTitle,
        Instant startsAt,
        Instant endsAt,
        @Min(1) @Max(10000) Integer attendeeCount,
        @Size(max = 2000) String notes,
        @Min(1) @Max(4) Integer wizardStep,
        Boolean attachmentReady,
        Long version) {}

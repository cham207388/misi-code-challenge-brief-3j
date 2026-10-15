package com.wsllclabs.memorialops.application;

import com.wsllclabs.memorialops.application.dto.ApplicantView;
import com.wsllclabs.memorialops.application.dto.ApplicationDetail;
import com.wsllclabs.memorialops.application.dto.ApplicationSummary;
import com.wsllclabs.memorialops.application.dto.EventView;
import com.wsllclabs.memorialops.identity.entity.AppUser;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public ApplicationSummary toSummary(ApplicationCase application, Applicant applicant, boolean includePii) {
        String name = applicant == null ? null : applicant.getFullName();
        if (!includePii) {
            name = name == null ? null : "Redacted";
        }
        return new ApplicationSummary(
                application.getId(),
                application.getReference(),
                application.getStatus(),
                name,
                application.getOwnerId(),
                application.getCemeteryId(),
                application.getVenueId(),
                application.getWizardStep(),
                application.getVersion(),
                VersionHeaders.etag(application.getVersion()),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }

    public ApplicationDetail toDetail(
            ApplicationCase application,
            Applicant applicant,
            EventRecord event,
            List<StatusHistory> history,
            AppUser viewer,
            CasePolicy policy) {
        boolean pii = policy.canSeeApplicantPii(viewer);
        ApplicantView applicantView = applicant == null
                ? null
                : new ApplicantView(
                        applicant.getId(),
                        pii ? applicant.getFullName() : "Redacted",
                        pii ? applicant.getEmail() : null,
                        pii ? applicant.getPhone() : null,
                        pii ? applicant.getOrganization() : null);
        EventView eventView = event == null
                ? null
                : new EventView(
                        event.getId(),
                        event.getTitle(),
                        event.getStartsAt(),
                        event.getEndsAt(),
                        event.getAttendeeCount(),
                        event.getNotes());
        return new ApplicationDetail(
                application.getId(),
                application.getReference(),
                application.getStatus(),
                applicantView,
                eventView,
                application.getOwnerId(),
                application.getCemeteryId(),
                application.getVenueId(),
                application.getEventCategoryId(),
                application.getWizardStep(),
                application.isAttachmentReady(),
                application.getRejectStage(),
                application.getLastReason(),
                application.getVersion(),
                VersionHeaders.etag(application.getVersion()),
                application.getCreatedAt(),
                application.getUpdatedAt(),
                history.stream()
                        .map(item -> (item.getFromStatus() == null
                                        ? "*"
                                        : item.getFromStatus().name())
                                + "->"
                                + item.getToStatus().name())
                        .toList());
    }
}

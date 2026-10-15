package com.wsllclabs.memorialops.application;

import com.wsllclabs.memorialops.identity.repository.AppUserRepository;
import com.wsllclabs.memorialops.shared.reference.ReferenceNumberService;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"local", "demo"})
public class DemoApplicationSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoApplicationSeeder.class);
    private static final UUID COORDINATOR = UUID.fromString("22222222-2222-2222-2222-222222222001");
    private static final UUID REVIEWER = UUID.fromString("22222222-2222-2222-2222-222222222002");
    private static final UUID ATTENDANT = UUID.fromString("22222222-2222-2222-2222-222222222005");
    private static final UUID OAKRIDGE = UUID.fromString("44444444-4444-4444-4444-444444444001");
    private static final UUID CHAPEL = UUID.fromString("55555555-5555-5555-5555-555555555001");
    private static final UUID MEMORIAL = UUID.fromString("66666666-6666-6666-6666-666666666001");

    private final ApplicationCaseRepository applicationCaseRepository;
    private final ApplicantRepository applicantRepository;
    private final EventRecordRepository eventRecordRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final EligibilityReviewRepository eligibilityReviewRepository;
    private final EventAssignmentRepository eventAssignmentRepository;
    private final AppUserRepository appUserRepository;
    private final ReferenceNumberService referenceNumberService;
    private final Clock clock;

    public DemoApplicationSeeder(
            ApplicationCaseRepository applicationCaseRepository,
            ApplicantRepository applicantRepository,
            EventRecordRepository eventRecordRepository,
            StatusHistoryRepository statusHistoryRepository,
            EligibilityReviewRepository eligibilityReviewRepository,
            EventAssignmentRepository eventAssignmentRepository,
            AppUserRepository appUserRepository,
            ReferenceNumberService referenceNumberService,
            Clock clock) {
        this.applicationCaseRepository = applicationCaseRepository;
        this.applicantRepository = applicantRepository;
        this.eventRecordRepository = eventRecordRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.eligibilityReviewRepository = eligibilityReviewRepository;
        this.eventAssignmentRepository = eventAssignmentRepository;
        this.appUserRepository = appUserRepository;
        this.referenceNumberService = referenceNumberService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (applicationCaseRepository.countByDeletedAtIsNull() > 0
                || appUserRepository.findById(COORDINATOR).isEmpty()) {
            return;
        }
        Instant now = Instant.now(clock);
        seed(ApplicationStatus.DRAFT, "Jordan Hale", "Sunrise remembrance", now.plus(2, ChronoUnit.DAYS), now, false);
        seed(ApplicationStatus.SUBMITTED, "Morgan Vale", "Chapel gathering", now.plus(3, ChronoUnit.DAYS), now, false);
        seed(ApplicationStatus.ELIGIBLE, "Ellis Park", "Oakridge committal", now.plus(4, ChronoUnit.DAYS), now, true);
        seed(
                ApplicationStatus.PENDING_APPROVAL,
                "Robin West",
                "Family memorial",
                now.plus(5, ChronoUnit.DAYS),
                now,
                true);
        seed(
                ApplicationStatus.CANCELLED,
                "Casey Frost",
                "Withdrawn visitation",
                now.plus(6, ChronoUnit.DAYS),
                now,
                false);
        log.info("Seeded demo applications across DRAFT, SUBMITTED, ELIGIBLE, PENDING_APPROVAL and CANCELLED");
    }

    private void seed(
            ApplicationStatus status,
            String applicantName,
            String title,
            Instant startsAt,
            Instant now,
            boolean assign) {
        Applicant applicant = applicantRepository.save(Applicant.create(applicantName, now));
        ApplicationCase application = ApplicationCase.draft(
                referenceNumberService.nextApplicationReference(), applicant.getId(), COORDINATOR, now);
        application.updateDraft(OAKRIDGE, CHAPEL, MEMORIAL, 4, true, COORDINATOR, now);
        if (status != ApplicationStatus.DRAFT) {
            application.transitionTo(
                    status, status == ApplicationStatus.CANCELLED ? "Family withdrew" : null, null, COORDINATOR, now);
        }
        application = applicationCaseRepository.save(application);
        EventRecord event = EventRecord.create(application.getId(), now);
        event.update(title, startsAt, startsAt.plus(90, ChronoUnit.MINUTES), 40, "Sprint 1 demo fixture", now);
        eventRecordRepository.save(event);
        statusHistoryRepository.save(
                StatusHistory.record(application.getId(), null, ApplicationStatus.DRAFT, COORDINATOR, null, now));
        if (status != ApplicationStatus.DRAFT) {
            statusHistoryRepository.save(StatusHistory.record(
                    application.getId(),
                    ApplicationStatus.DRAFT,
                    status,
                    COORDINATOR,
                    application.getLastReason(),
                    now));
        }
        if (status == ApplicationStatus.PENDING_APPROVAL || status == ApplicationStatus.ELIGIBLE) {
            eligibilityReviewRepository.save(
                    EligibilityReview.record(application.getId(), REVIEWER, "Eligible", "Seeded review", now));
        }
        if (assign) {
            eventAssignmentRepository.save(EventAssignment.assign(application.getId(), ATTENDANT));
        }
    }
}

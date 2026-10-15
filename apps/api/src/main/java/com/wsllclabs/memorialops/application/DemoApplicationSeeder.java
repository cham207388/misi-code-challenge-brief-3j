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
    private static final UUID OAKRIDGE = UUID.fromString("44444444-4444-4444-4444-444444444001");
    private static final UUID CHAPEL = UUID.fromString("55555555-5555-5555-5555-555555555001");
    private static final UUID MEMORIAL = UUID.fromString("66666666-6666-6666-6666-666666666001");

    private final ApplicationCaseRepository applicationCaseRepository;
    private final ApplicantRepository applicantRepository;
    private final EventRecordRepository eventRecordRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final AppUserRepository appUserRepository;
    private final ReferenceNumberService referenceNumberService;
    private final Clock clock;

    public DemoApplicationSeeder(
            ApplicationCaseRepository applicationCaseRepository,
            ApplicantRepository applicantRepository,
            EventRecordRepository eventRecordRepository,
            StatusHistoryRepository statusHistoryRepository,
            AppUserRepository appUserRepository,
            ReferenceNumberService referenceNumberService,
            Clock clock) {
        this.applicationCaseRepository = applicationCaseRepository;
        this.applicantRepository = applicantRepository;
        this.eventRecordRepository = eventRecordRepository;
        this.statusHistoryRepository = statusHistoryRepository;
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
        Applicant applicant = applicantRepository.save(Applicant.create("Jordan Hale", now));
        ApplicationCase application = ApplicationCase.draft(
                referenceNumberService.nextApplicationReference(), applicant.getId(), COORDINATOR, now);
        application.updateDraft(OAKRIDGE, CHAPEL, MEMORIAL, 4, true, COORDINATOR, now);
        application = applicationCaseRepository.save(application);
        EventRecord event = EventRecord.create(application.getId(), now);
        event.update(
                "Sunrise remembrance",
                now.plus(2, ChronoUnit.DAYS),
                now.plus(2, ChronoUnit.DAYS).plus(90, ChronoUnit.MINUTES),
                40,
                "Seeded draft for the intake wizard resume path",
                now);
        eventRecordRepository.save(event);
        statusHistoryRepository.save(
                StatusHistory.record(application.getId(), null, ApplicationStatus.DRAFT, COORDINATOR, null, now));
        log.info("Seeded demo application {}", application.getReference());
    }
}

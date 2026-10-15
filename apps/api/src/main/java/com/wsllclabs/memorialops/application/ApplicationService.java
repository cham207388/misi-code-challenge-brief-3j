package com.wsllclabs.memorialops.application;

import com.wsllclabs.memorialops.application.dto.ApplicationDetail;
import com.wsllclabs.memorialops.application.dto.ApplicationPageResponse;
import com.wsllclabs.memorialops.application.dto.CreateDraftRequest;
import com.wsllclabs.memorialops.application.dto.UpdateDraftRequest;
import com.wsllclabs.memorialops.audit.AuditService;
import com.wsllclabs.memorialops.exception.ConflictException;
import com.wsllclabs.memorialops.exception.NotFoundException;
import com.wsllclabs.memorialops.identity.entity.AppUser;
import com.wsllclabs.memorialops.identity.entity.RoleEntity;
import com.wsllclabs.memorialops.identity.service.CurrentUserService;
import com.wsllclabs.memorialops.shared.reference.ReferenceNumberService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class ApplicationService {

    private final ApplicationCaseRepository applicationCaseRepository;
    private final ApplicantRepository applicantRepository;
    private final EventRecordRepository eventRecordRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final EventAssignmentRepository eventAssignmentRepository;
    private final ReferenceNumberService referenceNumberService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;
    private final CasePolicy casePolicy;
    private final ApplicationMapper applicationMapper;
    private final Clock clock;

    public ApplicationService(
            ApplicationCaseRepository applicationCaseRepository,
            ApplicantRepository applicantRepository,
            EventRecordRepository eventRecordRepository,
            StatusHistoryRepository statusHistoryRepository,
            EventAssignmentRepository eventAssignmentRepository,
            ReferenceNumberService referenceNumberService,
            AuditService auditService,
            CurrentUserService currentUserService,
            CasePolicy casePolicy,
            ApplicationMapper applicationMapper,
            Clock clock) {
        this.applicationCaseRepository = applicationCaseRepository;
        this.applicantRepository = applicantRepository;
        this.eventRecordRepository = eventRecordRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.eventAssignmentRepository = eventAssignmentRepository;
        this.referenceNumberService = referenceNumberService;
        this.auditService = auditService;
        this.currentUserService = currentUserService;
        this.casePolicy = casePolicy;
        this.applicationMapper = applicationMapper;
        this.clock = clock;
    }

    @Transactional
    public ApplicationDetail createDraft(CreateDraftRequest request) {
        AppUser actor = currentUserService.requireCurrentUser();
        if (!casePolicy.canCreate(actor)) {
            throw new AccessDeniedException("Access denied");
        }
        Instant now = Instant.now(clock);
        String name = request == null
                        || request.applicantName() == null
                        || request.applicantName().isBlank()
                ? "Draft applicant"
                : request.applicantName().trim();
        Applicant applicant = applicantRepository.save(Applicant.create(name, now));
        ApplicationCase application = ApplicationCase.draft(
                referenceNumberService.nextApplicationReference(), applicant.getId(), actor.getId(), now);
        application = applicationCaseRepository.save(application);
        eventRecordRepository.save(EventRecord.create(application.getId(), now));
        statusHistoryRepository.save(
                StatusHistory.record(application.getId(), null, ApplicationStatus.DRAFT, actor.getId(), null, now));
        auditService.append(
                actor.getId(),
                primaryRole(actor),
                "APPLICATION_CREATED",
                "Application",
                application.getId(),
                null,
                "{\"status\":\"DRAFT\",\"reference\":\"" + application.getReference() + "\"}",
                currentIp());
        return detail(application.getId(), actor);
    }

    @Transactional(readOnly = true)
    public ApplicationPageResponse list(ApplicationStatus status, int page, int size) {
        AppUser actor = currentUserService.requireCurrentUser();
        if (!casePolicy.canList(actor)) {
            throw new AccessDeniedException("Access denied");
        }
        int safeSize = size <= 0 ? 20 : Math.min(size, 100);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ApplicationCase> result;
        if (casePolicy.hasRole(actor, CasePolicy.INTAKE)
                && casePolicy.rolesOf(actor).size() == 1) {
            result = status == null
                    ? applicationCaseRepository.findByDeletedAtIsNullAndOwnerId(actor.getId(), pageable)
                    : applicationCaseRepository.findByDeletedAtIsNullAndOwnerIdAndStatus(
                            actor.getId(), status, pageable);
        } else if (casePolicy.hasRole(actor, CasePolicy.ATTENDANT)
                && !casePolicy.hasRole(actor, CasePolicy.REVIEWER)
                && !casePolicy.hasRole(actor, CasePolicy.MANAGER)
                && !casePolicy.hasRole(actor, CasePolicy.AUDITOR)
                && !casePolicy.hasRole(actor, CasePolicy.INTAKE)) {
            List<UUID> assigned = eventAssignmentRepository.findByAttendantId(actor.getId()).stream()
                    .map(EventAssignment::getApplicationId)
                    .toList();
            result = assigned.isEmpty()
                    ? Page.empty(pageable)
                    : applicationCaseRepository.findByDeletedAtIsNullAndIdIn(assigned, pageable);
        } else if (casePolicy.schedulerOnlyEligiblePlus(actor)) {
            result = status == null
                    ? applicationCaseRepository.findByDeletedAtIsNullAndStatusIn(
                            ApplicationStatus.SCHEDULER_VISIBLE, pageable)
                    : applicationCaseRepository.findByDeletedAtIsNullAndStatus(status, pageable);
        } else {
            result = status == null
                    ? applicationCaseRepository.findByDeletedAtIsNull(pageable)
                    : applicationCaseRepository.findByDeletedAtIsNullAndStatus(status, pageable);
        }
        boolean pii = casePolicy.canSeeApplicantPii(actor);
        return new ApplicationPageResponse(
                result.getContent().stream()
                        .map(item -> applicationMapper.toSummary(
                                item,
                                applicantRepository
                                        .findById(item.getApplicantId())
                                        .orElse(null),
                                pii))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ApplicationDetail get(UUID id) {
        AppUser actor = currentUserService.requireCurrentUser();
        ApplicationCase application = requireVisible(id, actor);
        return detail(application.getId(), actor);
    }

    @Transactional
    public ApplicationDetail updateDraft(UUID id, UpdateDraftRequest request, String ifMatch) {
        AppUser actor = currentUserService.requireCurrentUser();
        ApplicationCase application = requireVisible(id, actor);
        if (!casePolicy.canEditDraft(actor, application)) {
            throw new AccessDeniedException("Access denied");
        }
        assertVersion(application, ifMatch, request.version(), actor);
        Instant now = Instant.now(clock);
        Applicant applicant = applicantRepository
                .findById(application.getApplicantId())
                .orElseThrow(() -> new NotFoundException("Applicant not found"));
        String previous = "{\"wizardStep\":" + application.getWizardStep() + "}";
        applicant.update(
                blankTo(request.applicantName(), applicant.getFullName()),
                request.applicantEmail(),
                request.applicantPhone(),
                request.organization(),
                now);
        EventRecord event = eventRecordRepository
                .findByApplicationId(application.getId())
                .orElseGet(() -> EventRecord.create(application.getId(), now));
        event.update(
                request.eventTitle(),
                request.startsAt(),
                request.endsAt(),
                request.attendeeCount(),
                request.notes(),
                now);
        application.updateDraft(
                request.cemeteryId(),
                request.venueId(),
                request.eventCategoryId(),
                request.wizardStep() == null ? application.getWizardStep() : request.wizardStep(),
                request.attachmentReady() == null ? application.isAttachmentReady() : request.attachmentReady(),
                actor.getId(),
                now);
        applicantRepository.save(applicant);
        eventRecordRepository.save(event);
        applicationCaseRepository.save(application);
        auditService.append(
                actor.getId(),
                primaryRole(actor),
                "APPLICATION_UPDATED",
                "Application",
                application.getId(),
                previous,
                "{\"wizardStep\":" + application.getWizardStep() + "}",
                currentIp());
        return detail(application.getId(), actor);
    }

    public ApplicationCase requireVisible(UUID id, AppUser actor) {
        ApplicationCase application = applicationCaseRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        if (!casePolicy.canRead(actor, application)) {
            throw new AccessDeniedException("Access denied");
        }
        return application;
    }

    public void assertVersion(ApplicationCase application, String ifMatch, Long bodyVersion, AppUser actor) {
        long expected = VersionHeaders.requireVersion(ifMatch, bodyVersion);
        if (expected != application.getVersion()) {
            throw new ConflictException(
                    "The record was changed by someone else",
                    application.getVersion(),
                    detail(application.getId(), actor));
        }
    }

    public ApplicationDetail detail(UUID id, AppUser actor) {
        ApplicationCase application = applicationCaseRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        return applicationMapper.toDetail(
                application,
                applicantRepository.findById(application.getApplicantId()).orElse(null),
                eventRecordRepository.findByApplicationId(id).orElse(null),
                statusHistoryRepository.findByApplicationIdOrderByOccurredAtAsc(id),
                actor,
                casePolicy);
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public static String primaryRole(AppUser actor) {
        return actor.getRoles().stream()
                .map(RoleEntity::getCode)
                .sorted()
                .findFirst()
                .orElse("");
    }

    public static String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getRemoteAddr();
        }
        return null;
    }
}

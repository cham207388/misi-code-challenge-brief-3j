package com.wsllclabs.memorialops.workflow;

import com.wsllclabs.memorialops.application.Applicant;
import com.wsllclabs.memorialops.application.ApplicantRepository;
import com.wsllclabs.memorialops.application.ApplicationCase;
import com.wsllclabs.memorialops.application.ApplicationCaseRepository;
import com.wsllclabs.memorialops.application.ApplicationService;
import com.wsllclabs.memorialops.application.ApplicationStatus;
import com.wsllclabs.memorialops.application.CasePolicy;
import com.wsllclabs.memorialops.application.EligibilityReview;
import com.wsllclabs.memorialops.application.EligibilityReviewRepository;
import com.wsllclabs.memorialops.application.EventRecord;
import com.wsllclabs.memorialops.application.EventRecordRepository;
import com.wsllclabs.memorialops.application.StatusHistory;
import com.wsllclabs.memorialops.application.StatusHistoryRepository;
import com.wsllclabs.memorialops.application.dto.ApplicationDetail;
import com.wsllclabs.memorialops.application.dto.AvailableAction;
import com.wsllclabs.memorialops.application.dto.TransitionRequest;
import com.wsllclabs.memorialops.audit.AuditService;
import com.wsllclabs.memorialops.exception.InvalidTransitionException;
import com.wsllclabs.memorialops.exception.RequestValidationException;
import com.wsllclabs.memorialops.exception.SeparationOfDutiesException;
import com.wsllclabs.memorialops.identity.entity.AppUser;
import com.wsllclabs.memorialops.identity.service.CurrentUserService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowService {

    private final TransitionTable transitionTable;
    private final ApplicationService applicationService;
    private final ApplicationCaseRepository applicationCaseRepository;
    private final ApplicantRepository applicantRepository;
    private final EventRecordRepository eventRecordRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final EligibilityReviewRepository eligibilityReviewRepository;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;
    private final CasePolicy casePolicy;
    private final WorkflowAfterWriteHook afterWriteHook;
    private final Clock clock;

    public WorkflowService(
            TransitionTable transitionTable,
            ApplicationService applicationService,
            ApplicationCaseRepository applicationCaseRepository,
            ApplicantRepository applicantRepository,
            EventRecordRepository eventRecordRepository,
            StatusHistoryRepository statusHistoryRepository,
            EligibilityReviewRepository eligibilityReviewRepository,
            AuditService auditService,
            CurrentUserService currentUserService,
            CasePolicy casePolicy,
            WorkflowAfterWriteHook afterWriteHook,
            Clock clock) {
        this.transitionTable = transitionTable;
        this.applicationService = applicationService;
        this.applicationCaseRepository = applicationCaseRepository;
        this.applicantRepository = applicantRepository;
        this.eventRecordRepository = eventRecordRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.eligibilityReviewRepository = eligibilityReviewRepository;
        this.auditService = auditService;
        this.currentUserService = currentUserService;
        this.casePolicy = casePolicy;
        this.afterWriteHook = afterWriteHook;
        this.clock = clock;
    }

    @Transactional
    public ApplicationDetail transition(UUID id, TransitionRequest request, String ifMatch) {
        AppUser actor = currentUserService.requireCurrentUser();
        ApplicationCase application = applicationService.requireVisible(id, actor);
        applicationService.assertVersion(application, ifMatch, request.version(), actor);
        TransitionRule rule = transitionTable
                .find(application.getStatus(), request.target())
                .orElseThrow(() -> new InvalidTransitionException(
                        "Transition from " + application.getStatus() + " to " + request.target() + " is not allowed",
                        allowedNames(application, actor)));
        if (!casePolicy.hasRole(actor, rule.requiredRole())) {
            throw new AccessDeniedException("Access denied");
        }
        evaluateGuards(rule, application, actor, request.reason());
        Instant now = Instant.now(clock);
        ApplicationStatus from = application.getStatus();
        application.transitionTo(request.target(), request.reason(), rule.rejectStage(), actor.getId(), now);
        applicationCaseRepository.save(application);
        statusHistoryRepository.save(StatusHistory.record(
                application.getId(), from, request.target(), actor.getId(), request.reason(), now));
        if (request.target() == ApplicationStatus.ELIGIBLE
                || request.target() == ApplicationStatus.NEEDS_INFORMATION
                || (request.target() == ApplicationStatus.REJECTED && "ELIGIBILITY".equals(rule.rejectStage()))) {
            eligibilityReviewRepository.save(EligibilityReview.record(
                    application.getId(), actor.getId(), request.target().name(), request.reason(), now));
        }
        auditService.append(
                actor.getId(),
                ApplicationService.primaryRole(actor),
                "STATUS_CHANGED",
                "Application",
                application.getId(),
                "{\"status\":\"" + from + "\"}",
                "{\"status\":\"" + request.target() + "\"}",
                ApplicationService.currentIp());
        afterWriteHook.afterPersist();
        return applicationService.detail(application.getId(), actor);
    }

    @Transactional(readOnly = true)
    public List<AvailableAction> actions(UUID id) {
        AppUser actor = currentUserService.requireCurrentUser();
        ApplicationCase application = applicationService.requireVisible(id, actor);
        List<AvailableAction> actions = new ArrayList<>();
        Set<String> roles = casePolicy.rolesOf(actor);
        for (TransitionRule rule : transitionTable.rules()) {
            if (rule.from() != application.getStatus()) {
                continue;
            }
            if (!roles.contains(rule.requiredRole())) {
                actions.add(new AvailableAction(rule.to(), false, "Requires role " + rule.requiredRole()));
                continue;
            }
            try {
                evaluateGuards(rule, application, actor, "placeholder-reason");
                boolean needsReason = rule.guards().contains(TransitionGuard.REASON_REQUIRED);
                actions.add(new AvailableAction(rule.to(), true, needsReason ? "Reason required" : "Allowed"));
            } catch (RuntimeException exception) {
                actions.add(new AvailableAction(rule.to(), false, exception.getMessage()));
            }
        }
        return actions;
    }

    public TransitionTable table() {
        return transitionTable;
    }

    void evaluateGuards(TransitionRule rule, ApplicationCase application, AppUser actor, String reason) {
        Applicant applicant =
                applicantRepository.findById(application.getApplicantId()).orElse(null);
        EventRecord event =
                eventRecordRepository.findByApplicationId(application.getId()).orElse(null);
        for (TransitionGuard guard : rule.guards()) {
            switch (guard) {
                case FORM_VALID -> {
                    if (!formValid(applicant, event)) {
                        throw new RequestValidationException(
                                "Form is incomplete: applicant name, event title, 30-300 minute window and attendees are required");
                    }
                }
                case ATTACHMENT_READY -> {
                    if (!application.isAttachmentReady()) {
                        throw new RequestValidationException("A required attachment must be recorded before submit");
                    }
                }
                case REASON_REQUIRED -> {
                    if (reason == null || reason.isBlank()) {
                        throw new RequestValidationException("A reason is required for this transition");
                    }
                }
                case SEPARATION_OF_DUTIES -> {
                    eligibilityReviewRepository
                            .findFirstByApplicationIdOrderByCreatedAtDesc(application.getId())
                            .map(EligibilityReview::getReviewerId)
                            .filter(reviewerId -> reviewerId.equals(actor.getId()))
                            .ifPresent(ignored -> {
                                throw new SeparationOfDutiesException(
                                        "The eligibility reviewer cannot approve or reject the same case");
                            });
                }
            }
        }
    }

    private static boolean formValid(Applicant applicant, EventRecord event) {
        if (applicant == null
                || applicant.getFullName() == null
                || applicant.getFullName().isBlank()
                || "Draft applicant".equalsIgnoreCase(applicant.getFullName())
                || event == null
                || event.getTitle() == null
                || event.getTitle().isBlank()
                || event.getStartsAt() == null
                || event.getEndsAt() == null
                || event.getAttendeeCount() == null
                || event.getAttendeeCount() < 1) {
            return false;
        }
        long minutes = Duration.between(event.getStartsAt(), event.getEndsAt()).toMinutes();
        return minutes >= 30 && minutes <= 300;
    }

    private List<String> allowedNames(ApplicationCase application, AppUser actor) {
        return transitionTable.allowedTargets(application.getStatus(), List.copyOf(casePolicy.rolesOf(actor))).stream()
                .map(Enum::name)
                .toList();
    }
}

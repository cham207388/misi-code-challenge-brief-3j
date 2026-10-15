package com.wsllclabs.memorialops.workflow;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wsllclabs.memorialops.application.Applicant;
import com.wsllclabs.memorialops.application.ApplicantRepository;
import com.wsllclabs.memorialops.application.ApplicationCase;
import com.wsllclabs.memorialops.application.ApplicationCaseRepository;
import com.wsllclabs.memorialops.application.ApplicationService;
import com.wsllclabs.memorialops.application.ApplicationStatus;
import com.wsllclabs.memorialops.application.CasePolicy;
import com.wsllclabs.memorialops.application.EligibilityReview;
import com.wsllclabs.memorialops.application.EligibilityReviewRepository;
import com.wsllclabs.memorialops.application.EventAssignmentRepository;
import com.wsllclabs.memorialops.application.EventRecord;
import com.wsllclabs.memorialops.application.EventRecordRepository;
import com.wsllclabs.memorialops.application.StatusHistoryRepository;
import com.wsllclabs.memorialops.application.dto.TransitionRequest;
import com.wsllclabs.memorialops.audit.AuditService;
import com.wsllclabs.memorialops.exception.ConflictException;
import com.wsllclabs.memorialops.exception.InvalidTransitionException;
import com.wsllclabs.memorialops.exception.RequestValidationException;
import com.wsllclabs.memorialops.exception.SeparationOfDutiesException;
import com.wsllclabs.memorialops.identity.entity.AppUser;
import com.wsllclabs.memorialops.identity.entity.RoleEntity;
import com.wsllclabs.memorialops.identity.service.CurrentUserService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowServiceTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ApplicationCaseRepository applicationCaseRepository;

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private EventRecordRepository eventRecordRepository;

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @Mock
    private EligibilityReviewRepository eligibilityReviewRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private CurrentUserService currentUserService;

    private WorkflowService workflowService;
    private AppUser manager;
    private ApplicationCase pending;

    @BeforeEach
    void setUp() {
        CasePolicy policy = new CasePolicy(org.mockito.Mockito.mock(EventAssignmentRepository.class));
        workflowService = new WorkflowService(
                new TransitionTable(),
                applicationService,
                applicationCaseRepository,
                applicantRepository,
                eventRecordRepository,
                statusHistoryRepository,
                eligibilityReviewRepository,
                auditService,
                currentUserService,
                policy,
                new WorkflowAfterWriteHook(),
                Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC));
        manager = user("manager", UUID.fromString("22222222-2222-2222-2222-222222222004"), "OPERATIONS_MANAGER");
        pending = ApplicationCase.draft("APP-2026-00001", UUID.randomUUID(), UUID.randomUUID(), Instant.now());
        ReflectionTestUtils.setField(pending, "status", ApplicationStatus.PENDING_APPROVAL);
        ReflectionTestUtils.setField(pending, "version", 3L);
    }

    @ParameterizedTest
    @CsvSource({"DRAFT,APPROVED", "DRAFT,CANCELLED", "CLOSED,SUBMITTED", "REJECTED,ELIGIBLE", "CANCELLED,DRAFT"})
    void unknownPairIsUnprocessable(ApplicationStatus from, ApplicationStatus to) {
        ApplicationCase application =
                ApplicationCase.draft("APP-2026-00002", UUID.randomUUID(), UUID.randomUUID(), Instant.now());
        ReflectionTestUtils.setField(application, "status", from);
        when(currentUserService.requireCurrentUser()).thenReturn(manager);
        when(applicationService.requireVisible(any(), any())).thenReturn(application);

        assertThatThrownBy(() -> workflowService.transition(
                        application.getId(), new TransitionRequest(to, "because", 0L), "\"0\""))
                .isInstanceOf(InvalidTransitionException.class);
        verify(applicationCaseRepository, never()).save(any());
    }

    @Test
    void wrongRoleIsForbidden() {
        when(currentUserService.requireCurrentUser()).thenReturn(manager);
        ApplicationCase draft =
                ApplicationCase.draft("APP-2026-00003", UUID.randomUUID(), manager.getId(), Instant.now());
        when(applicationService.requireVisible(any(), any())).thenReturn(draft);

        assertThatThrownBy(() -> workflowService.transition(
                        draft.getId(), new TransitionRequest(ApplicationStatus.SUBMITTED, null, 0L), "\"0\""))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void staleVersionConflicts() {
        when(currentUserService.requireCurrentUser()).thenReturn(manager);
        when(applicationService.requireVisible(any(), any())).thenReturn(pending);
        org.mockito.Mockito.doThrow(new ConflictException("stale", 3, pending))
                .when(applicationService)
                .assertVersion(eq(pending), any(), any(), eq(manager));

        assertThatThrownBy(() -> workflowService.transition(
                        pending.getId(), new TransitionRequest(ApplicationStatus.APPROVED, null, 1L), "\"1\""))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void reasonRequiredWithoutReasonIsBadRequest() {
        when(currentUserService.requireCurrentUser()).thenReturn(manager);
        when(applicationService.requireVisible(any(), any())).thenReturn(pending);

        assertThatThrownBy(() -> workflowService.transition(
                        pending.getId(), new TransitionRequest(ApplicationStatus.REJECTED, "  ", 3L), "\"3\""))
                .isInstanceOf(RequestValidationException.class);
    }

    @Test
    void reviewerCannotApproveOwnCase() {
        UUID reviewerId = UUID.fromString("22222222-2222-2222-2222-222222222009");
        AppUser dual = user("dualrole", reviewerId, "OPERATIONS_MANAGER");
        when(currentUserService.requireCurrentUser()).thenReturn(dual);
        when(applicationService.requireVisible(any(), any())).thenReturn(pending);
        when(eligibilityReviewRepository.findFirstByApplicationIdOrderByCreatedAtDesc(pending.getId()))
                .thenReturn(Optional.of(
                        EligibilityReview.record(pending.getId(), reviewerId, "ELIGIBLE", null, Instant.now())));

        assertThatThrownBy(() -> workflowService.transition(
                        pending.getId(), new TransitionRequest(ApplicationStatus.APPROVED, null, 3L), "\"3\""))
                .isInstanceOf(SeparationOfDutiesException.class);
    }

    @Test
    void approveWritesHistoryAndAudit() {
        when(currentUserService.requireCurrentUser()).thenReturn(manager);
        when(applicationService.requireVisible(any(), any())).thenReturn(pending);
        when(eligibilityReviewRepository.findFirstByApplicationIdOrderByCreatedAtDesc(any()))
                .thenReturn(Optional.of(EligibilityReview.record(
                        pending.getId(),
                        UUID.fromString("22222222-2222-2222-2222-222222222002"),
                        "ELIGIBLE",
                        null,
                        Instant.now())));
        when(applicationCaseRepository.save(pending)).thenReturn(pending);

        workflowService.transition(
                pending.getId(), new TransitionRequest(ApplicationStatus.APPROVED, null, 3L), "\"3\"");

        verify(statusHistoryRepository).save(any());
        verify(auditService)
                .append(
                        any(),
                        anyString(),
                        eq("STATUS_CHANGED"),
                        eq("Application"),
                        eq(pending.getId()),
                        any(),
                        any(),
                        any());
        verify(applicationService).detail(pending.getId(), manager);
    }

    @Test
    void submitRequiresCompleteForm() {
        AppUser coordinator =
                user("coordinator", UUID.fromString("22222222-2222-2222-2222-222222222001"), "INTAKE_COORDINATOR");
        ApplicationCase draft =
                ApplicationCase.draft("APP-2026-00004", UUID.randomUUID(), coordinator.getId(), Instant.now());
        when(currentUserService.requireCurrentUser()).thenReturn(coordinator);
        when(applicationService.requireVisible(any(), any())).thenReturn(draft);
        when(applicantRepository.findById(draft.getApplicantId()))
                .thenReturn(Optional.of(Applicant.create("Draft applicant", Instant.now())));
        EventRecord event = EventRecord.create(draft.getId(), Instant.now());
        event.update("Title", Instant.now(), Instant.now().plus(90, ChronoUnit.MINUTES), 10, null, Instant.now());
        when(eventRecordRepository.findByApplicationId(draft.getId())).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> workflowService.transition(
                        draft.getId(), new TransitionRequest(ApplicationStatus.SUBMITTED, null, 0L), "\"0\""))
                .isInstanceOf(RequestValidationException.class);
    }

    private static AppUser user(String username, UUID id, String role) {
        AppUser user = org.mockito.Mockito.mock(AppUser.class);
        RoleEntity roleEntity = org.mockito.Mockito.mock(RoleEntity.class);
        when(roleEntity.getCode()).thenReturn(role);
        when(user.getId()).thenReturn(id);
        when(user.getUsername()).thenReturn(username);
        when(user.getRoles()).thenReturn(Set.of(roleEntity));
        return user;
    }
}

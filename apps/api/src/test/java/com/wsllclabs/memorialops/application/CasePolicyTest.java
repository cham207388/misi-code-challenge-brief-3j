package com.wsllclabs.memorialops.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.wsllclabs.memorialops.identity.entity.AppUser;
import com.wsllclabs.memorialops.identity.entity.RoleEntity;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CasePolicyTest {

    private final EventAssignmentRepository assignments = Mockito.mock(EventAssignmentRepository.class);
    private final CasePolicy policy = new CasePolicy(assignments);

    @Test
    void intakeOwnsDraftsAndAdminCannotRead() {
        UUID owner = UUID.randomUUID();
        ApplicationCase draft = ApplicationCase.draft("APP-2026-00009", UUID.randomUUID(), owner, Instant.now());
        AppUser intake = user(owner, CasePolicy.INTAKE);
        AppUser admin = user(UUID.randomUUID(), CasePolicy.ADMIN);
        AppUser scheduler = user(UUID.randomUUID(), CasePolicy.SCHEDULER);
        assertThat(policy.canCreate(intake)).isTrue();
        assertThat(policy.canEditDraft(intake, draft)).isTrue();
        assertThat(policy.canRead(intake, draft)).isTrue();
        assertThat(policy.canRead(admin, draft)).isFalse();
        assertThat(policy.canRead(scheduler, draft)).isFalse();
        assertThat(policy.canSeeApplicantPii(user(UUID.randomUUID(), CasePolicy.AUDITOR)))
                .isFalse();
        assertThat(policy.schedulerOnlyEligiblePlus(scheduler)).isTrue();
    }

    @Test
    void attendantReadsAssignedEventsOnly() {
        UUID attendantId = UUID.randomUUID();
        ApplicationCase application =
                ApplicationCase.draft("APP-2026-00010", UUID.randomUUID(), UUID.randomUUID(), Instant.now());
        when(assignments.existsByApplicationIdAndAttendantId(application.getId(), attendantId))
                .thenReturn(true);
        assertThat(policy.canRead(user(attendantId, CasePolicy.ATTENDANT), application))
                .isTrue();
        assertThat(policy.canList(user(attendantId, CasePolicy.ATTENDANT))).isTrue();
    }

    private static AppUser user(UUID id, String role) {
        AppUser user = Mockito.mock(AppUser.class);
        RoleEntity roleEntity = Mockito.mock(RoleEntity.class);
        when(roleEntity.getCode()).thenReturn(role);
        when(user.getId()).thenReturn(id);
        when(user.getRoles()).thenReturn(Set.of(roleEntity));
        return user;
    }
}

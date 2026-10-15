package com.wsllclabs.memorialops.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import com.wsllclabs.memorialops.application.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class TransitionTableTest {

    private final TransitionTable table = new TransitionTable();

    @ParameterizedTest
    @CsvSource({
        "DRAFT,SUBMITTED,INTAKE_COORDINATOR",
        "SUBMITTED,NEEDS_INFORMATION,ELIGIBILITY_REVIEWER",
        "NEEDS_INFORMATION,SUBMITTED,INTAKE_COORDINATOR",
        "SUBMITTED,ELIGIBLE,ELIGIBILITY_REVIEWER",
        "SUBMITTED,REJECTED,ELIGIBILITY_REVIEWER",
        "ELIGIBLE,TENTATIVELY_RESERVED,SCHEDULING_OFFICER",
        "TENTATIVELY_RESERVED,PENDING_APPROVAL,SCHEDULING_OFFICER",
        "PENDING_APPROVAL,APPROVED,OPERATIONS_MANAGER",
        "PENDING_APPROVAL,REJECTED,OPERATIONS_MANAGER",
        "APPROVED,PERMITTED,OPERATIONS_MANAGER",
        "PERMITTED,CHECKED_IN,EVENT_ATTENDANT",
        "CHECKED_IN,EVENT_COMPLETED,EVENT_ATTENDANT",
        "EVENT_COMPLETED,CLOSED,OPERATIONS_MANAGER",
        "SUBMITTED,CANCELLED,OPERATIONS_MANAGER",
        "ELIGIBLE,CANCELLED,OPERATIONS_MANAGER",
        "TENTATIVELY_RESERVED,CANCELLED,OPERATIONS_MANAGER",
        "PENDING_APPROVAL,CANCELLED,OPERATIONS_MANAGER",
        "APPROVED,CANCELLED,OPERATIONS_MANAGER",
        "PERMITTED,CANCELLED,OPERATIONS_MANAGER"
    })
    void allowedPairsAreRegistered(ApplicationStatus from, ApplicationStatus to, String role) {
        assertThat(table.find(from, to))
                .isPresent()
                .get()
                .extracting(TransitionRule::requiredRole)
                .isEqualTo(role);
    }

    @ParameterizedTest
    @EnumSource(ApplicationStatus.class)
    void unknownPairsAreAbsent(ApplicationStatus from) {
        for (ApplicationStatus to : ApplicationStatus.values()) {
            boolean expected = table.rules().stream().anyMatch(rule -> rule.from() == from && rule.to() == to);
            assertThat(table.find(from, to).isPresent()).isEqualTo(expected);
        }
    }

    @Test
    void closedAndRejectedHaveNoSuccessors() {
        assertThat(table.allowedTargets(ApplicationStatus.CLOSED)).isEmpty();
        assertThat(table.allowedTargets(ApplicationStatus.REJECTED)).isEmpty();
        assertThat(table.allowedTargets(ApplicationStatus.CANCELLED)).isEmpty();
        assertThat(table.allowedTargets(ApplicationStatus.DRAFT)).containsExactly(ApplicationStatus.SUBMITTED);
    }
}

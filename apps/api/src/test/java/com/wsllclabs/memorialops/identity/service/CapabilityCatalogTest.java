package com.wsllclabs.memorialops.identity.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CapabilityCatalogTest {

    @Test
    void dualRoleCombinesReviewAndApproval() {
        assertThat(CapabilityCatalog.capabilitiesFor(List.of("ELIGIBILITY_REVIEWER", "OPERATIONS_MANAGER")))
                .contains(
                        CapabilityCatalog.REVIEW_QUEUE,
                        CapabilityCatalog.APPROVAL_DECIDE,
                        CapabilityCatalog.DASHBOARD_VIEW);
    }

    @Test
    void unknownRoleYieldsNothing() {
        assertThat(CapabilityCatalog.capabilitiesFor(List.of("NO_SUCH_ROLE"))).isEmpty();
    }

    @Test
    void coordinatorAndAdminCapabilities() {
        assertThat(CapabilityCatalog.capabilitiesFor(List.of("INTAKE_COORDINATOR")))
                .contains(
                        CapabilityCatalog.APPLICATION_CREATE,
                        CapabilityCatalog.APPLICATION_READ,
                        CapabilityCatalog.INFO_REQUESTS);
        assertThat(CapabilityCatalog.capabilitiesFor(List.of("SYSTEM_ADMIN")))
                .contains(
                        CapabilityCatalog.ADMIN_USERS,
                        CapabilityCatalog.SCENARIO_CONTROL,
                        CapabilityCatalog.GEO_MANAGE);
        assertThat(CapabilityCatalog.capabilitiesFor(List.of("EVENT_ATTENDANT")))
                .contains(CapabilityCatalog.CHECK_IN, CapabilityCatalog.ROSTER_VIEW)
                .doesNotContain(CapabilityCatalog.APPROVAL_DECIDE);
        assertThat(CapabilityCatalog.capabilitiesFor(List.of("COMPLIANCE_AUDITOR")))
                .contains(CapabilityCatalog.REPORTS_VIEW)
                .doesNotContain(CapabilityCatalog.APPLICATION_CREATE);
        assertThat(CapabilityCatalog.capabilitiesFor(List.of("SCHEDULING_OFFICER")))
                .contains(CapabilityCatalog.CALENDAR_VIEW, CapabilityCatalog.RESOURCE_PLAN);
    }
}

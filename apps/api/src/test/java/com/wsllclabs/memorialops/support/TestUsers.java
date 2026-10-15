package com.wsllclabs.memorialops.support;

import java.util.List;

public record TestUsers(String username, String password, String role) {

    public static final TestUsers COORDINATOR = new TestUsers("coordinator", "Coordinator!2026", "INTAKE_COORDINATOR");
    public static final TestUsers REVIEWER = new TestUsers("reviewer", "Reviewer!2026", "ELIGIBILITY_REVIEWER");
    public static final TestUsers SCHEDULER = new TestUsers("scheduler", "Scheduler!2026", "SCHEDULING_OFFICER");
    public static final TestUsers MANAGER = new TestUsers("manager", "Manager!2026", "OPERATIONS_MANAGER");
    public static final TestUsers ATTENDANT = new TestUsers("attendant1", "Attendant!2026", "EVENT_ATTENDANT");
    public static final TestUsers AUDITOR = new TestUsers("auditor", "Auditor!2026", "COMPLIANCE_AUDITOR");
    public static final TestUsers ADMIN = new TestUsers("admin", "Admin!2026", "SYSTEM_ADMIN");
    public static final TestUsers DUALROLE = new TestUsers("dualrole", "DualRole!2026", "ELIGIBILITY_REVIEWER");

    public static List<TestUsers> staff() {
        return List.of(COORDINATOR, REVIEWER, SCHEDULER, MANAGER, ATTENDANT, AUDITOR, ADMIN, DUALROLE);
    }
}

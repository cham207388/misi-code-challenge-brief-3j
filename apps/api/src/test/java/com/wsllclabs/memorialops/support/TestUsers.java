package com.wsllclabs.memorialops.support;

public record TestUsers(String username, String password) {

    public static final TestUsers COORDINATOR = new TestUsers("coordinator", "Coordinator!2026");
    public static final TestUsers REVIEWER = new TestUsers("reviewer", "Reviewer!2026");
    public static final TestUsers MANAGER = new TestUsers("manager", "Manager!2026");
    public static final TestUsers DUALROLE = new TestUsers("dualrole", "DualRole!2026");
}

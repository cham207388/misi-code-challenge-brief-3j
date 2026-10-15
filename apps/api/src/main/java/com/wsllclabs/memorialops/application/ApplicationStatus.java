package com.wsllclabs.memorialops.application;

import java.util.EnumSet;
import java.util.Set;

public enum ApplicationStatus {
    DRAFT,
    SUBMITTED,
    NEEDS_INFORMATION,
    ELIGIBLE,
    TENTATIVELY_RESERVED,
    PENDING_APPROVAL,
    APPROVED,
    PERMITTED,
    CHECKED_IN,
    EVENT_COMPLETED,
    CLOSED,
    REJECTED,
    CANCELLED;

    public static final Set<ApplicationStatus> SCHEDULER_VISIBLE = EnumSet.of(
            ELIGIBLE,
            TENTATIVELY_RESERVED,
            PENDING_APPROVAL,
            APPROVED,
            PERMITTED,
            CHECKED_IN,
            EVENT_COMPLETED,
            CLOSED,
            REJECTED,
            CANCELLED);

    public static final Set<ApplicationStatus> TERMINAL = EnumSet.of(CLOSED, REJECTED, CANCELLED);
}

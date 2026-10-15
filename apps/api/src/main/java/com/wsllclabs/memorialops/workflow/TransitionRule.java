package com.wsllclabs.memorialops.workflow;

import com.wsllclabs.memorialops.application.ApplicationStatus;
import java.util.List;

public record TransitionRule(
        ApplicationStatus from,
        ApplicationStatus to,
        String requiredRole,
        List<TransitionGuard> guards,
        String rejectStage) {

    public TransitionRule(
            ApplicationStatus from, ApplicationStatus to, String requiredRole, TransitionGuard... guards) {
        this(from, to, requiredRole, List.of(guards), null);
    }

    public static TransitionRule reject(
            ApplicationStatus from, String requiredRole, String rejectStage, TransitionGuard... guards) {
        return new TransitionRule(from, ApplicationStatus.REJECTED, requiredRole, List.of(guards), rejectStage);
    }
}

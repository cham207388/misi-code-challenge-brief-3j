package com.wsllclabs.memorialops.workflow;

import com.wsllclabs.memorialops.application.ApplicationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TransitionTable {

    public static final String INTAKE = "INTAKE_COORDINATOR";
    public static final String REVIEWER = "ELIGIBILITY_REVIEWER";
    public static final String SCHEDULER = "SCHEDULING_OFFICER";
    public static final String MANAGER = "OPERATIONS_MANAGER";
    public static final String ATTENDANT = "EVENT_ATTENDANT";

    private static final List<TransitionRule> RULES = List.of(
            new TransitionRule(
                    ApplicationStatus.DRAFT,
                    ApplicationStatus.SUBMITTED,
                    INTAKE,
                    TransitionGuard.FORM_VALID,
                    TransitionGuard.ATTACHMENT_READY),
            new TransitionRule(
                    ApplicationStatus.SUBMITTED,
                    ApplicationStatus.NEEDS_INFORMATION,
                    REVIEWER,
                    TransitionGuard.REASON_REQUIRED),
            new TransitionRule(
                    ApplicationStatus.NEEDS_INFORMATION,
                    ApplicationStatus.SUBMITTED,
                    INTAKE,
                    TransitionGuard.FORM_VALID),
            new TransitionRule(ApplicationStatus.SUBMITTED, ApplicationStatus.ELIGIBLE, REVIEWER),
            TransitionRule.reject(
                    ApplicationStatus.SUBMITTED, REVIEWER, "ELIGIBILITY", TransitionGuard.REASON_REQUIRED),
            new TransitionRule(ApplicationStatus.ELIGIBLE, ApplicationStatus.TENTATIVELY_RESERVED, SCHEDULER),
            new TransitionRule(ApplicationStatus.TENTATIVELY_RESERVED, ApplicationStatus.PENDING_APPROVAL, SCHEDULER),
            new TransitionRule(
                    ApplicationStatus.PENDING_APPROVAL,
                    ApplicationStatus.APPROVED,
                    MANAGER,
                    TransitionGuard.SEPARATION_OF_DUTIES),
            TransitionRule.reject(
                    ApplicationStatus.PENDING_APPROVAL,
                    MANAGER,
                    "APPROVAL",
                    TransitionGuard.REASON_REQUIRED,
                    TransitionGuard.SEPARATION_OF_DUTIES),
            new TransitionRule(ApplicationStatus.APPROVED, ApplicationStatus.PERMITTED, MANAGER),
            new TransitionRule(ApplicationStatus.PERMITTED, ApplicationStatus.CHECKED_IN, ATTENDANT),
            new TransitionRule(ApplicationStatus.CHECKED_IN, ApplicationStatus.EVENT_COMPLETED, ATTENDANT),
            new TransitionRule(ApplicationStatus.EVENT_COMPLETED, ApplicationStatus.CLOSED, MANAGER),
            cancel(ApplicationStatus.SUBMITTED),
            cancel(ApplicationStatus.ELIGIBLE),
            cancel(ApplicationStatus.TENTATIVELY_RESERVED),
            cancel(ApplicationStatus.PENDING_APPROVAL),
            cancel(ApplicationStatus.APPROVED),
            cancel(ApplicationStatus.PERMITTED));

    private static TransitionRule cancel(ApplicationStatus from) {
        return new TransitionRule(from, ApplicationStatus.CANCELLED, MANAGER, TransitionGuard.REASON_REQUIRED);
    }

    public List<TransitionRule> rules() {
        return RULES;
    }

    public Optional<TransitionRule> find(ApplicationStatus from, ApplicationStatus to) {
        return RULES.stream()
                .filter(rule -> rule.from() == from && rule.to() == to)
                .findFirst();
    }

    public List<ApplicationStatus> allowedTargets(ApplicationStatus from, List<String> roles) {
        return RULES.stream()
                .filter(rule -> rule.from() == from && roles.contains(rule.requiredRole()))
                .map(TransitionRule::to)
                .distinct()
                .toList();
    }

    public List<ApplicationStatus> allowedTargets(ApplicationStatus from) {
        return RULES.stream()
                .filter(rule -> rule.from() == from)
                .map(TransitionRule::to)
                .distinct()
                .toList();
    }
}

package com.wsllclabs.memorialops.application;

import com.wsllclabs.memorialops.identity.entity.AppUser;
import com.wsllclabs.memorialops.identity.entity.RoleEntity;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CasePolicy {

    public static final String INTAKE = "INTAKE_COORDINATOR";
    public static final String REVIEWER = "ELIGIBILITY_REVIEWER";
    public static final String SCHEDULER = "SCHEDULING_OFFICER";
    public static final String MANAGER = "OPERATIONS_MANAGER";
    public static final String ATTENDANT = "EVENT_ATTENDANT";
    public static final String AUDITOR = "COMPLIANCE_AUDITOR";
    public static final String ADMIN = "SYSTEM_ADMIN";

    private final EventAssignmentRepository eventAssignmentRepository;

    public CasePolicy(EventAssignmentRepository eventAssignmentRepository) {
        this.eventAssignmentRepository = eventAssignmentRepository;
    }

    public Set<String> rolesOf(AppUser user) {
        return user.getRoles().stream().map(RoleEntity::getCode).collect(Collectors.toSet());
    }

    public boolean hasRole(AppUser user, String role) {
        return rolesOf(user).contains(role);
    }

    public boolean canList(AppUser user) {
        Set<String> roles = rolesOf(user);
        return roles.contains(INTAKE)
                || roles.contains(REVIEWER)
                || roles.contains(SCHEDULER)
                || roles.contains(MANAGER)
                || roles.contains(AUDITOR)
                || roles.contains(ATTENDANT);
    }

    public boolean canRead(AppUser user, ApplicationCase application) {
        Set<String> roles = rolesOf(user);
        if (roles.contains(ADMIN) && roles.size() == 1) {
            return false;
        }
        if (roles.contains(INTAKE) && application.getOwnerId().equals(user.getId())) {
            return true;
        }
        if (roles.contains(ATTENDANT)
                && eventAssignmentRepository.existsByApplicationIdAndAttendantId(application.getId(), user.getId())) {
            return true;
        }
        if (roles.contains(SCHEDULER)
                && !roles.contains(REVIEWER)
                && !roles.contains(MANAGER)
                && !roles.contains(INTAKE)
                && !roles.contains(AUDITOR)
                && !ApplicationStatus.SCHEDULER_VISIBLE.contains(application.getStatus())) {
            return false;
        }
        return roles.contains(REVIEWER)
                || roles.contains(SCHEDULER)
                || roles.contains(MANAGER)
                || roles.contains(AUDITOR);
    }

    public boolean canCreate(AppUser user) {
        return hasRole(user, INTAKE);
    }

    public boolean canEditDraft(AppUser user, ApplicationCase application) {
        return hasRole(user, INTAKE)
                && application.getOwnerId().equals(user.getId())
                && (application.getStatus() == ApplicationStatus.DRAFT
                        || application.getStatus() == ApplicationStatus.NEEDS_INFORMATION);
    }

    public boolean canSeeApplicantPii(AppUser user) {
        Set<String> roles = rolesOf(user);
        return !roles.contains(ATTENDANT) && !roles.contains(AUDITOR);
    }

    public boolean schedulerOnlyEligiblePlus(AppUser user) {
        Set<String> roles = rolesOf(user);
        return roles.contains(SCHEDULER)
                && !roles.contains(REVIEWER)
                && !roles.contains(MANAGER)
                && !roles.contains(INTAKE)
                && !roles.contains(AUDITOR);
    }
}

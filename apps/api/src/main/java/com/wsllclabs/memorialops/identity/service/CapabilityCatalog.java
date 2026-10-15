package com.wsllclabs.memorialops.identity.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CapabilityCatalog {

    public static final String DASHBOARD_VIEW = "DASHBOARD_VIEW";
    public static final String APPLICATION_READ = "APPLICATION_READ";
    public static final String APPLICATION_CREATE = "APPLICATION_CREATE";
    public static final String REVIEW_QUEUE = "REVIEW_QUEUE";
    public static final String INFO_REQUESTS = "INFO_REQUESTS";
    public static final String CALENDAR_VIEW = "CALENDAR_VIEW";
    public static final String VENUE_AVAILABILITY = "VENUE_AVAILABILITY";
    public static final String RESOURCE_PLAN = "RESOURCE_PLAN";
    public static final String APPROVAL_DECIDE = "APPROVAL_DECIDE";
    public static final String PERMIT_MANAGE = "PERMIT_MANAGE";
    public static final String NOTIFICATION_MANAGE = "NOTIFICATION_MANAGE";
    public static final String ROSTER_VIEW = "ROSTER_VIEW";
    public static final String CHECK_IN = "CHECK_IN";
    public static final String INCIDENT_MANAGE = "INCIDENT_MANAGE";
    public static final String REPORTS_VIEW = "REPORTS_VIEW";
    public static final String ADMIN_VENUES = "ADMIN_VENUES";
    public static final String ADMIN_USERS = "ADMIN_USERS";
    public static final String INTEGRATION_HUB = "INTEGRATION_HUB";
    public static final String REGISTRY_VERIFY = "REGISTRY_VERIFY";
    public static final String CALENDAR_SYNC = "CALENDAR_SYNC";
    public static final String MESSAGING_MONITOR = "MESSAGING_MONITOR";
    public static final String DOCUMENT_VAULT = "DOCUMENT_VAULT";
    public static final String WEATHER_VIEW = "WEATHER_VIEW";
    public static final String IMPORT_MANAGE = "IMPORT_MANAGE";
    public static final String GEO_MANAGE = "GEO_MANAGE";
    public static final String SCENARIO_CONTROL = "SCENARIO_CONTROL";

    private static final Map<String, List<String>> BY_ROLE = Map.of(
            "INTAKE_COORDINATOR",
            List.of(
                    DASHBOARD_VIEW,
                    APPLICATION_READ,
                    APPLICATION_CREATE,
                    INFO_REQUESTS,
                    DOCUMENT_VAULT),
            "ELIGIBILITY_REVIEWER",
            List.of(DASHBOARD_VIEW, APPLICATION_READ, REVIEW_QUEUE, INFO_REQUESTS, REGISTRY_VERIFY, DOCUMENT_VAULT),
            "SCHEDULING_OFFICER",
            List.of(
                    DASHBOARD_VIEW,
                    APPLICATION_READ,
                    CALENDAR_VIEW,
                    VENUE_AVAILABILITY,
                    RESOURCE_PLAN,
                    CALENDAR_SYNC,
                    IMPORT_MANAGE,
                    DOCUMENT_VAULT),
            "OPERATIONS_MANAGER",
            List.of(
                    DASHBOARD_VIEW,
                    APPLICATION_READ,
                    APPROVAL_DECIDE,
                    PERMIT_MANAGE,
                    NOTIFICATION_MANAGE,
                    ROSTER_VIEW,
                    CHECK_IN,
                    INCIDENT_MANAGE,
                    REPORTS_VIEW,
                    MESSAGING_MONITOR,
                    WEATHER_VIEW,
                    DOCUMENT_VAULT),
            "EVENT_ATTENDANT",
            List.of(DASHBOARD_VIEW, ROSTER_VIEW, CHECK_IN, INCIDENT_MANAGE),
            "COMPLIANCE_AUDITOR",
            List.of(DASHBOARD_VIEW, APPLICATION_READ, REPORTS_VIEW),
            "SYSTEM_ADMIN",
            List.of(
                    DASHBOARD_VIEW,
                    ADMIN_VENUES,
                    ADMIN_USERS,
                    INTEGRATION_HUB,
                    REGISTRY_VERIFY,
                    CALENDAR_SYNC,
                    MESSAGING_MONITOR,
                    DOCUMENT_VAULT,
                    WEATHER_VIEW,
                    IMPORT_MANAGE,
                    GEO_MANAGE,
                    SCENARIO_CONTROL,
                    NOTIFICATION_MANAGE));

    private CapabilityCatalog() {}

    public static Set<String> capabilitiesFor(Iterable<String> roleCodes) {
        Set<String> capabilities = new LinkedHashSet<>();
        for (String role : roleCodes) {
            capabilities.addAll(BY_ROLE.getOrDefault(role, List.of()));
        }
        return capabilities;
    }
}

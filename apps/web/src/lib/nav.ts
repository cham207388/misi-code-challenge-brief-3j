export type NavItem = {
  number: string;
  path: string;
  title: string;
  capability: string;
  group: string;
};

export const NAV_ITEMS: NavItem[] = [
  { number: "01", path: "/dashboard", title: "Operations Dashboard", capability: "DASHBOARD_VIEW", group: "Work" },
  { number: "02", path: "/applications", title: "Application Register", capability: "APPLICATION_READ", group: "Work" },
  { number: "03", path: "/applications/new/applicant", title: "New Application", capability: "APPLICATION_CREATE", group: "Work" },
  { number: "05", path: "/reviews", title: "Eligibility Reviews", capability: "REVIEW_QUEUE", group: "Work" },
  { number: "06", path: "/information-requests", title: "Information Requests", capability: "INFO_REQUESTS", group: "Work" },
  { number: "07", path: "/calendar", title: "Event Calendar", capability: "CALENDAR_VIEW", group: "Scheduling" },
  { number: "08", path: "/venues/availability", title: "Venue Availability", capability: "VENUE_AVAILABILITY", group: "Scheduling" },
  { number: "09", path: "/resources", title: "Resource Planner", capability: "RESOURCE_PLAN", group: "Scheduling" },
  { number: "10", path: "/approvals", title: "Approval Workspace", capability: "APPROVAL_DECIDE", group: "Operations" },
  { number: "11", path: "/permits", title: "Permit Center", capability: "PERMIT_MANAGE", group: "Operations" },
  { number: "12", path: "/notifications", title: "Notification Center", capability: "NOTIFICATION_MANAGE", group: "Operations" },
  { number: "13", path: "/roster", title: "Daily Event Roster", capability: "ROSTER_VIEW", group: "Event day" },
  { number: "14", path: "/check-in", title: "Event Check-In", capability: "CHECK_IN", group: "Event day" },
  { number: "15", path: "/incidents", title: "Incident Register", capability: "INCIDENT_MANAGE", group: "Event day" },
  { number: "16", path: "/reports", title: "Reports & Exports", capability: "REPORTS_VIEW", group: "Operations" },
  { number: "17", path: "/admin/venues", title: "Administration: Venues", capability: "ADMIN_VENUES", group: "Admin" },
  { number: "18", path: "/admin/users-rules", title: "Users & Rules", capability: "ADMIN_USERS", group: "Admin" },
  { number: "19", path: "/integrations", title: "Integration Hub", capability: "INTEGRATION_HUB", group: "Integrations" },
  { number: "20", path: "/integrations/registry", title: "Applicant Registry", capability: "REGISTRY_VERIFY", group: "Integrations" },
  { number: "21", path: "/integrations/calendar", title: "Regional Calendar Sync", capability: "CALENDAR_SYNC", group: "Integrations" },
  { number: "22", path: "/integrations/messaging", title: "Messaging Monitor", capability: "MESSAGING_MONITOR", group: "Integrations" },
  { number: "23", path: "/documents", title: "Document Vault", capability: "DOCUMENT_VAULT", group: "Integrations" },
  { number: "24", path: "/integrations/weather", title: "Weather & Advisories", capability: "WEATHER_VIEW", group: "Integrations" },
  { number: "25", path: "/integrations/imports", title: "Data Import", capability: "IMPORT_MANAGE", group: "Integrations" },
  { number: "26", path: "/integrations/geo", title: "Geolocation & Map", capability: "GEO_MANAGE", group: "Integrations" },
];

export const PAGE_COPY: Record<string, string> = {
  "/dashboard": "KPI cards, today's events, pending actions and deadlines will bind to live queries in Sprint 1+.",
  "/applications": "Server-paged register with filters, saved views and CSV export.",
  "/applications/new/applicant": "Four-step intake wizard with draft autosave. Later sprints add validation and documents.",
  "/reviews": "Eligibility queue, checklist, registry evidence and request-information actions.",
  "/information-requests": "Clarifications, due dates, responses and overdue tracking.",
  "/calendar": "Month, week and day views of persisted reservations, blackouts and external events.",
  "/venues/availability": "Capacity, hours, buffers and live collision validation.",
  "/resources": "Allocate chairs, tents, attendants and AV kits against remaining stock.",
  "/approvals": "Pending decisions with sign-off and separation of duties.",
  "/permits": "Versioned printable PDF permits with QR tokens.",
  "/notifications": "Template-driven outbox, delivery status and retry.",
  "/roster": "Date and site roster, readiness flags and printable run-of-show.",
  "/check-in": "Verify permit tokens and record arrival and departure.",
  "/incidents": "Severity, photos, owners, due dates and audited resolution.",
  "/reports": "Calculated KPIs, drilldowns and formula-safe CSV exports.",
  "/admin/venues": "Cemeteries, venues, hours, blackouts and inventories.",
  "/admin/users-rules": "Roles, active accounts, rule thresholds and the audit viewer.",
  "/integrations": "Seven live integration cards, masked config and test connections.",
  "/integrations/registry": "Applicant reference lookup against the fictional registry mock.",
  "/integrations/calendar": "OAuth2 calendar sync, conflicts and reconciliation.",
  "/integrations/messaging": "Email/SMS queue, signed webhook receipts and safe retry.",
  "/documents": "Private object-storage attachments with expiring download links.",
  "/integrations/weather": "Cached forecasts, stale indicators and severe-weather acknowledgement.",
  "/integrations/imports": "Partner CSV / SFTP import with partial-success and row errors.",
  "/integrations/geo": "Geocode candidates, confirmation and an offline-safe map.",
};

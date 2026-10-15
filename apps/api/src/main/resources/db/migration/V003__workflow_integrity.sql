-- Sprint 1: status CHECKs, FK indexes, append-only grants, reference data.

ALTER TABLE application
    ADD COLUMN attachment_ready boolean NOT NULL DEFAULT false,
    ADD COLUMN reject_stage text,
    ADD COLUMN last_reason text;

ALTER TABLE application
    ADD CONSTRAINT application_status_chk CHECK (status IN (
        'DRAFT',
        'SUBMITTED',
        'NEEDS_INFORMATION',
        'ELIGIBLE',
        'TENTATIVELY_RESERVED',
        'PENDING_APPROVAL',
        'APPROVED',
        'PERMITTED',
        'CHECKED_IN',
        'EVENT_COMPLETED',
        'CLOSED',
        'REJECTED',
        'CANCELLED'
    ));

ALTER TABLE application
    ADD CONSTRAINT application_reject_stage_chk CHECK (
        reject_stage IS NULL OR reject_stage IN ('ELIGIBILITY', 'APPROVAL')
    );

ALTER TABLE reservation
    ADD CONSTRAINT reservation_status_chk CHECK (status IN ('TENTATIVE', 'CONFIRMED', 'RELEASED'));

ALTER TABLE resource_allocation
    ADD CONSTRAINT resource_allocation_status_chk CHECK (status IN ('RESERVED', 'RELEASED'));

ALTER TABLE permit
    ADD CONSTRAINT permit_status_chk CHECK (status IN ('DRAFT', 'ACTIVE', 'CANCELLED', 'REVOKED'));

ALTER TABLE permit_version
    ADD CONSTRAINT permit_version_status_chk CHECK (status IN ('ACTIVE', 'SUPERSEDED', 'CANCELLED', 'REVOKED'));

ALTER TABLE eligibility_review
    ADD CONSTRAINT eligibility_review_outcome_chk CHECK (outcome IN ('ELIGIBLE', 'NEEDS_INFORMATION', 'REJECTED'));

ALTER TABLE approval_decision
    ADD CONSTRAINT approval_decision_chk CHECK (decision IN ('APPROVED', 'REJECTED'));

ALTER TABLE incident
    ADD CONSTRAINT incident_severity_chk CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));

ALTER TABLE incident
    ADD CONSTRAINT incident_status_chk CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'));

CREATE INDEX application_owner_idx ON application (owner_id);
CREATE INDEX application_applicant_idx ON application (applicant_id);
CREATE INDEX application_cemetery_idx ON application (cemetery_id);
CREATE INDEX application_venue_idx ON application (venue_id);
CREATE INDEX application_category_idx ON application (event_category_id);
CREATE INDEX event_application_idx ON event (application_id);
CREATE INDEX reservation_application_idx ON reservation (application_id);
CREATE INDEX eligibility_review_application_idx ON eligibility_review (application_id);
CREATE INDEX eligibility_review_reviewer_idx ON eligibility_review (reviewer_id);
CREATE INDEX approval_decision_application_idx ON approval_decision (application_id);
CREATE INDEX status_history_application_idx ON status_history (application_id, occurred_at DESC);
CREATE INDEX event_assignment_attendant_idx ON event_assignment (attendant_id);
CREATE INDEX user_role_role_idx ON user_role (role_id);
CREATE INDEX venue_cemetery_idx ON venue (cemetery_id);
CREATE INDEX venue_hours_venue_idx ON venue_hours (venue_id);
CREATE INDEX venue_blackout_venue_idx ON venue_blackout (venue_id);

INSERT INTO cemetery (id, code, name, time_zone, version, created_at, updated_at)
VALUES
    ('44444444-4444-4444-4444-444444444001', 'OAK', 'Oakridge Memorial Grounds', 'America/New_York', 0, now(), now()),
    ('44444444-4444-4444-4444-444444444002', 'LAKE', 'Lakeside National Cemetery', 'America/Chicago', 0, now(), now());

INSERT INTO venue (id, cemetery_id, code, name, capacity, setup_buffer_minutes, teardown_buffer_minutes, address_line, version, created_at, updated_at)
VALUES
    ('55555555-5555-5555-5555-555555555001', '44444444-4444-4444-4444-444444444001', 'CHAPEL', 'Oakridge Chapel', 80, 30, 30, '100 Memorial Way', 0, now(), now()),
    ('55555555-5555-5555-5555-555555555002', '44444444-4444-4444-4444-444444444001', 'LAWN', 'East Lawn', 200, 30, 30, '100 Memorial Way', 0, now(), now()),
    ('55555555-5555-5555-5555-555555555003', '44444444-4444-4444-4444-444444444002', 'TERRACE', 'Lakeside Terrace', 120, 30, 30, '40 Shore Road', 0, now(), now());

INSERT INTO event_category (id, code, name, version, created_at, updated_at)
VALUES
    ('66666666-6666-6666-6666-666666666001', 'MEMORIAL', 'Memorial Ceremony', 0, now(), now()),
    ('66666666-6666-6666-6666-666666666002', 'COMMITAL', 'Committal Service', 0, now(), now()),
    ('66666666-6666-6666-6666-666666666003', 'WREATH', 'Wreath Laying', 0, now(), now());

-- Seeded demo accounts. Plaintext passwords appear only in the README.
INSERT INTO role (id, code, name) VALUES
    ('11111111-1111-1111-1111-111111111001', 'INTAKE_COORDINATOR', 'Intake Coordinator'),
    ('11111111-1111-1111-1111-111111111002', 'ELIGIBILITY_REVIEWER', 'Eligibility Reviewer'),
    ('11111111-1111-1111-1111-111111111003', 'SCHEDULING_OFFICER', 'Scheduling Officer'),
    ('11111111-1111-1111-1111-111111111004', 'OPERATIONS_MANAGER', 'Operations Manager'),
    ('11111111-1111-1111-1111-111111111005', 'EVENT_ATTENDANT', 'Event Attendant'),
    ('11111111-1111-1111-1111-111111111006', 'COMPLIANCE_AUDITOR', 'Compliance Auditor'),
    ('11111111-1111-1111-1111-111111111007', 'SYSTEM_ADMIN', 'System Admin');

INSERT INTO app_user (id, username, password_hash, display_name, email, active, version, created_at, updated_at)
VALUES
    ('22222222-2222-2222-2222-222222222001', 'coordinator', '$2b$10$5u0HtKNQRhsq.a0TpDVQF.s8ncseUa930.SB37M3ZYLFyXJO.cb2y', 'Casey Coordinator', 'casey.coordinator@willowmere.example', true, 0, now(), now()),
    ('22222222-2222-2222-2222-222222222002', 'reviewer', '$2b$10$Wo5RkyAAVu2Z1U6GodcsHuKS65mcBSSabId2LcS4uoDRAcMcY8fxO', 'Riley Reviewer', 'riley.reviewer@willowmere.example', true, 0, now(), now()),
    ('22222222-2222-2222-2222-222222222003', 'scheduler', '$2b$10$cIKcSwG7M6kyNMSqudGOee7Ujsyw/bOohWew/kaind7DdyjGLOz4C', 'Sam Scheduler', 'sam.scheduler@willowmere.example', true, 0, now(), now()),
    ('22222222-2222-2222-2222-222222222004', 'manager', '$2b$10$w9EW4EbIVezgygMS1eyfheMUvExblrlykf6Xctx1lf.p9uX7g638W', 'Morgan Manager', 'morgan.manager@willowmere.example', true, 0, now(), now()),
    ('22222222-2222-2222-2222-222222222005', 'attendant1', '$2b$10$NFRZigutUL0YFIFFJ1juQeW5eqBswShPeX0kTDlHPm7EZ9wHHl7ia', 'Avery Attendant', 'avery.attendant@willowmere.example', true, 0, now(), now()),
    ('22222222-2222-2222-2222-222222222006', 'attendant2', '$2b$10$NFRZigutUL0YFIFFJ1juQeW5eqBswShPeX0kTDlHPm7EZ9wHHl7ia', 'Blake Attendant', 'blake.attendant@willowmere.example', true, 0, now(), now()),
    ('22222222-2222-2222-2222-222222222007', 'auditor', '$2b$10$Ou4Z8Eo4HxuD6nPQ6PNpoekMblP/a0D2xEqi.brnmMXaJ7k2H9j9i', 'Quinn Auditor', 'quinn.auditor@willowmere.example', true, 0, now(), now()),
    ('22222222-2222-2222-2222-222222222008', 'admin', '$2b$10$RK0oxPSRTrCBS91HhALCPOacpFh2na2VqEmThMLV3f7apc6tvX0pe', 'Alex Admin', 'alex.admin@willowmere.example', true, 0, now(), now());

INSERT INTO user_role (user_id, role_id) VALUES
    ('22222222-2222-2222-2222-222222222001', '11111111-1111-1111-1111-111111111001'),
    ('22222222-2222-2222-2222-222222222002', '11111111-1111-1111-1111-111111111002'),
    ('22222222-2222-2222-2222-222222222003', '11111111-1111-1111-1111-111111111003'),
    ('22222222-2222-2222-2222-222222222004', '11111111-1111-1111-1111-111111111004'),
    ('22222222-2222-2222-2222-222222222005', '11111111-1111-1111-1111-111111111005'),
    ('22222222-2222-2222-2222-222222222006', '11111111-1111-1111-1111-111111111005'),
    ('22222222-2222-2222-2222-222222222007', '11111111-1111-1111-1111-111111111006'),
    ('22222222-2222-2222-2222-222222222008', '11111111-1111-1111-1111-111111111007');

INSERT INTO business_rule (id, rule_key, value_json) VALUES
    ('33333333-3333-3333-3333-333333333001', 'checkin.window.minutes', '{"before": 60, "after": 30}'),
    ('33333333-3333-3333-3333-333333333002', 'reservation.defaultBufferMinutes', '{"setup": 30, "teardown": 30}'),
    ('33333333-3333-3333-3333-333333333003', 'informationRequest.dueDays', '{"days": 5}'),
    ('33333333-3333-3333-3333-333333333004', 'registry.override.allowed', '{"Flagged": true, "Unknown": false}'),
    ('33333333-3333-3333-3333-333333333005', 'weather.cacheTtlMinutes', '{"minutes": 15}');

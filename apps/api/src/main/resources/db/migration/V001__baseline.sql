CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE role (
    id uuid PRIMARY KEY,
    code text NOT NULL UNIQUE,
    name text NOT NULL
);

CREATE TABLE app_user (
    id uuid PRIMARY KEY,
    username citext NOT NULL UNIQUE,
    password_hash text NOT NULL,
    display_name text NOT NULL,
    email citext NOT NULL,
    active boolean NOT NULL DEFAULT true,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by uuid,
    updated_by uuid,
    deleted_at timestamptz
);

CREATE TABLE user_role (
    user_id uuid NOT NULL REFERENCES app_user (id),
    role_id uuid NOT NULL REFERENCES role (id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE cemetery (
    id uuid PRIMARY KEY,
    code text NOT NULL UNIQUE,
    name text NOT NULL,
    time_zone text NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by uuid,
    updated_by uuid,
    deleted_at timestamptz
);

CREATE TABLE venue (
    id uuid PRIMARY KEY,
    cemetery_id uuid NOT NULL REFERENCES cemetery (id),
    code text NOT NULL,
    name text NOT NULL,
    capacity integer NOT NULL CHECK (capacity >= 1),
    setup_buffer_minutes integer NOT NULL DEFAULT 30 CHECK (setup_buffer_minutes >= 0),
    teardown_buffer_minutes integer NOT NULL DEFAULT 30 CHECK (teardown_buffer_minutes >= 0),
    address_line text,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by uuid,
    updated_by uuid,
    deleted_at timestamptz,
    UNIQUE (cemetery_id, code)
);

CREATE TABLE venue_hours (
    id uuid PRIMARY KEY,
    venue_id uuid NOT NULL REFERENCES venue (id),
    weekday smallint NOT NULL CHECK (weekday BETWEEN 1 AND 7),
    opens_local time NOT NULL,
    closes_local time NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    UNIQUE (venue_id, weekday)
);

CREATE TABLE venue_blackout (
    id uuid PRIMARY KEY,
    venue_id uuid NOT NULL REFERENCES venue (id),
    kind text NOT NULL CHECK (kind IN ('SOFT', 'HARD')),
    starts_at timestamptz NOT NULL,
    ends_at timestamptz NOT NULL,
    reason text NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by uuid,
    updated_by uuid,
    deleted_at timestamptz,
    CHECK (ends_at > starts_at)
);

CREATE TABLE resource_type (
    id uuid PRIMARY KEY,
    code text NOT NULL UNIQUE,
    name text NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz
);

CREATE TABLE resource_inventory (
    id uuid PRIMARY KEY,
    cemetery_id uuid NOT NULL REFERENCES cemetery (id),
    resource_type_id uuid NOT NULL REFERENCES resource_type (id),
    quantity integer NOT NULL CHECK (quantity >= 0),
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    UNIQUE (cemetery_id, resource_type_id)
);

CREATE TABLE event_category (
    id uuid PRIMARY KEY,
    code text NOT NULL UNIQUE,
    name text NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz
);

CREATE TABLE applicant (
    id uuid PRIMARY KEY,
    full_name text NOT NULL,
    email citext,
    phone text,
    organization text,
    registry_reference text,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by uuid,
    updated_by uuid,
    deleted_at timestamptz
);

CREATE TABLE application (
    id uuid PRIMARY KEY,
    reference text NOT NULL UNIQUE,
    status text NOT NULL,
    applicant_id uuid NOT NULL REFERENCES applicant (id),
    owner_id uuid NOT NULL REFERENCES app_user (id),
    cemetery_id uuid REFERENCES cemetery (id),
    venue_id uuid REFERENCES venue (id),
    event_category_id uuid REFERENCES event_category (id),
    wizard_step smallint NOT NULL DEFAULT 1,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by uuid,
    updated_by uuid,
    deleted_at timestamptz
);

CREATE TABLE event (
    id uuid PRIMARY KEY,
    application_id uuid NOT NULL UNIQUE REFERENCES application (id),
    title text,
    starts_at timestamptz,
    ends_at timestamptz,
    attendee_count integer CHECK (attendee_count IS NULL OR attendee_count >= 1),
    notes text,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CHECK (ends_at IS NULL OR starts_at IS NULL OR ends_at > starts_at),
    CHECK (
        starts_at IS NULL
        OR ends_at IS NULL
        OR EXTRACT(EPOCH FROM (ends_at - starts_at)) / 60 BETWEEN 30 AND 300
    )
);

CREATE TABLE reservation (
    id uuid PRIMARY KEY,
    application_id uuid NOT NULL REFERENCES application (id),
    venue_id uuid NOT NULL REFERENCES venue (id),
    status text NOT NULL,
    starts_at timestamptz NOT NULL,
    ends_at timestamptz NOT NULL,
    buffer_start timestamptz NOT NULL,
    buffer_end timestamptz NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by uuid,
    updated_by uuid,
    deleted_at timestamptz,
    CHECK (ends_at > starts_at),
    EXCLUDE USING gist (
        venue_id WITH =,
        tstzrange(buffer_start, buffer_end, '[)') WITH &&
    ) WHERE (status IN ('TENTATIVE', 'CONFIRMED') AND deleted_at IS NULL)
);

CREATE TABLE resource_allocation (
    id uuid PRIMARY KEY,
    reservation_id uuid NOT NULL REFERENCES reservation (id),
    resource_type_id uuid NOT NULL REFERENCES resource_type (id),
    quantity integer NOT NULL CHECK (quantity >= 1),
    status text NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE eligibility_review (
    id uuid PRIMARY KEY,
    application_id uuid NOT NULL REFERENCES application (id),
    reviewer_id uuid NOT NULL REFERENCES app_user (id),
    outcome text NOT NULL,
    notes text,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE information_request (
    id uuid PRIMARY KEY,
    application_id uuid NOT NULL REFERENCES application (id),
    requested_by uuid NOT NULL REFERENCES app_user (id),
    due_at timestamptz NOT NULL,
    items jsonb NOT NULL DEFAULT '[]'::jsonb,
    status text NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE information_response (
    id uuid PRIMARY KEY,
    information_request_id uuid NOT NULL REFERENCES information_request (id),
    responder_id uuid NOT NULL REFERENCES app_user (id),
    body text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE approval_decision (
    id uuid PRIMARY KEY,
    application_id uuid NOT NULL REFERENCES application (id),
    decision text NOT NULL,
    notes text,
    signed_by uuid NOT NULL REFERENCES app_user (id),
    signed_role text NOT NULL,
    signed_at timestamptz NOT NULL,
    signature_hash text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE permit (
    id uuid PRIMARY KEY,
    application_id uuid NOT NULL UNIQUE REFERENCES application (id),
    reference text NOT NULL UNIQUE,
    status text NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE permit_version (
    id uuid PRIMARY KEY,
    permit_id uuid NOT NULL REFERENCES permit (id),
    version_number integer NOT NULL,
    status text NOT NULL,
    issuer_id uuid NOT NULL REFERENCES app_user (id),
    object_key text,
    checksum_sha256 text,
    token text NOT NULL,
    issued_at timestamptz NOT NULL,
    justification text,
    UNIQUE (permit_id, version_number)
);

CREATE TABLE notification (
    id uuid PRIMARY KEY,
    application_id uuid REFERENCES application (id),
    template_code text,
    channel text NOT NULL,
    status text NOT NULL,
    idempotency_key text UNIQUE,
    payload jsonb NOT NULL DEFAULT '{}'::jsonb,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE delivery_event (
    id uuid PRIMARY KEY,
    notification_id uuid NOT NULL REFERENCES notification (id),
    external_event_id text UNIQUE,
    message_id text,
    status text NOT NULL,
    occurred_at timestamptz NOT NULL,
    signature_valid boolean,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE check_in (
    id uuid PRIMARY KEY,
    application_id uuid NOT NULL REFERENCES application (id),
    attendant_id uuid NOT NULL REFERENCES app_user (id),
    kind text NOT NULL CHECK (kind IN ('IN', 'OUT')),
    recorded_at timestamptz NOT NULL,
    idempotency_key text UNIQUE,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE incident (
    id uuid PRIMARY KEY,
    application_id uuid NOT NULL REFERENCES application (id),
    severity text NOT NULL,
    status text NOT NULL,
    summary text NOT NULL,
    owner_id uuid REFERENCES app_user (id),
    due_at timestamptz,
    corrective_action text,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by uuid,
    updated_by uuid,
    deleted_at timestamptz
);

CREATE TABLE attachment (
    id uuid PRIMARY KEY,
    application_id uuid REFERENCES application (id),
    incident_id uuid REFERENCES incident (id),
    file_name text NOT NULL,
    mime_type text NOT NULL,
    byte_size integer NOT NULL CHECK (byte_size > 0 AND byte_size <= 5242880),
    checksum_sha256 text NOT NULL,
    object_key text NOT NULL UNIQUE,
    uploaded_by uuid NOT NULL REFERENCES app_user (id),
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE comment (
    id uuid PRIMARY KEY,
    application_id uuid NOT NULL REFERENCES application (id),
    author_id uuid NOT NULL REFERENCES app_user (id),
    body text NOT NULL,
    parent_id uuid REFERENCES comment (id),
    revision integer NOT NULL DEFAULT 1,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE status_history (
    id uuid PRIMARY KEY,
    application_id uuid NOT NULL REFERENCES application (id),
    from_status text,
    to_status text NOT NULL,
    actor_id uuid NOT NULL REFERENCES app_user (id),
    reason text,
    occurred_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE audit_event (
    id uuid PRIMARY KEY,
    occurred_at timestamptz NOT NULL DEFAULT now(),
    actor_id uuid REFERENCES app_user (id),
    actor_role text,
    action text NOT NULL,
    entity_type text NOT NULL,
    entity_id uuid,
    before_state jsonb,
    after_state jsonb,
    correlation_id text,
    ip text
);

CREATE FUNCTION prevent_audit_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_event is append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_event_no_update
    BEFORE UPDATE OR DELETE ON audit_event
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_mutation();

CREATE TABLE reference_counter (
    kind text NOT NULL,
    year integer NOT NULL,
    last_value integer NOT NULL,
    PRIMARY KEY (kind, year)
);

CREATE TABLE business_rule (
    id uuid PRIMARY KEY,
    rule_key text NOT NULL UNIQUE,
    value_json jsonb NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    updated_by uuid
);

CREATE TABLE notification_template (
    id uuid PRIMARY KEY,
    code text NOT NULL UNIQUE,
    channel text NOT NULL,
    subject text,
    body text NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE idempotency_key (
    key text PRIMARY KEY,
    request_hash text NOT NULL,
    response_status integer,
    response_body jsonb,
    created_at timestamptz NOT NULL DEFAULT now(),
    expires_at timestamptz NOT NULL
);

CREATE TABLE shedlock (
    name varchar(64) PRIMARY KEY,
    lock_until timestamptz NOT NULL,
    locked_at timestamptz NOT NULL,
    locked_by varchar(255) NOT NULL
);

CREATE TABLE saved_view (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES app_user (id),
    name text NOT NULL,
    page_key text NOT NULL,
    query_string text NOT NULL,
    is_default boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    UNIQUE (user_id, page_key, name)
);

CREATE TABLE integration_attempt (
    id uuid PRIMARY KEY,
    adapter text NOT NULL,
    direction text NOT NULL,
    request_id text NOT NULL,
    correlation_id text,
    application_id uuid REFERENCES application (id),
    endpoint text NOT NULL,
    status_code integer,
    latency_ms integer,
    retry_attempt integer NOT NULL DEFAULT 0,
    request_excerpt text,
    response_excerpt text,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE registry_verification (
    id uuid PRIMARY KEY,
    application_id uuid REFERENCES application (id),
    reference_id text NOT NULL,
    matched_name text,
    eligibility_status text NOT NULL,
    verified_at timestamptz NOT NULL,
    override_reason text,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE oauth_token_cache (
    client_key text PRIMARY KEY,
    access_token text NOT NULL,
    token_type text NOT NULL,
    expires_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE sync_cursor (
    adapter text PRIMARY KEY,
    cursor_value text,
    last_success_at timestamptz
);

CREATE TABLE external_calendar_event (
    id uuid PRIMARY KEY,
    external_id text NOT NULL UNIQUE,
    venue_code text,
    start_utc timestamptz NOT NULL,
    end_utc timestamptz NOT NULL,
    modified_utc timestamptz NOT NULL,
    payload jsonb NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE reconciliation_item (
    id uuid PRIMARY KEY,
    external_calendar_event_id uuid NOT NULL REFERENCES external_calendar_event (id),
    reservation_id uuid REFERENCES reservation (id),
    status text NOT NULL,
    notes text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE import_batch (
    id uuid PRIMARY KEY,
    source text NOT NULL,
    checksum text NOT NULL,
    total_rows integer NOT NULL DEFAULT 0,
    valid_rows integer NOT NULL DEFAULT 0,
    invalid_rows integer NOT NULL DEFAULT 0,
    duplicate_rows integer NOT NULL DEFAULT 0,
    status text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    completed_at timestamptz
);

CREATE TABLE import_row_error (
    id uuid PRIMARY KEY,
    import_batch_id uuid NOT NULL REFERENCES import_batch (id),
    row_number integer NOT NULL,
    message text NOT NULL,
    raw_row text
);

CREATE TABLE partner_event (
    id uuid PRIMARY KEY,
    import_batch_id uuid NOT NULL REFERENCES import_batch (id),
    partner_event_id text NOT NULL,
    site_code text NOT NULL,
    start_utc timestamptz NOT NULL,
    end_utc timestamptz NOT NULL,
    attendee_count integer NOT NULL,
    updated_at_utc timestamptz NOT NULL,
    UNIQUE (partner_event_id, updated_at_utc)
);

CREATE TABLE weather_snapshot (
    id uuid PRIMARY KEY,
    venue_id uuid NOT NULL REFERENCES venue (id),
    forecast_date date NOT NULL,
    fetched_at timestamptz NOT NULL,
    source_timestamp timestamptz,
    payload jsonb NOT NULL,
    severe boolean NOT NULL DEFAULT false,
    UNIQUE (venue_id, forecast_date)
);

CREATE TABLE weather_ack (
    id uuid PRIMARY KEY,
    weather_snapshot_id uuid NOT NULL REFERENCES weather_snapshot (id),
    application_id uuid REFERENCES application (id),
    acknowledged_by uuid NOT NULL REFERENCES app_user (id),
    note text,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE geocode_result (
    id uuid PRIMARY KEY,
    venue_id uuid NOT NULL REFERENCES venue (id),
    label text NOT NULL,
    latitude numeric(9, 6),
    longitude numeric(9, 6),
    confidence numeric(3, 2),
    selected boolean NOT NULL DEFAULT false,
    verified boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE permit_token_key (
    kid text PRIMARY KEY,
    secret text NOT NULL,
    active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE event_assignment (
    id uuid PRIMARY KEY,
    application_id uuid NOT NULL REFERENCES application (id),
    attendant_id uuid NOT NULL REFERENCES app_user (id),
    UNIQUE (application_id, attendant_id)
);

CREATE TABLE readiness_checklist_item (
    id uuid PRIMARY KEY,
    application_id uuid NOT NULL REFERENCES application (id),
    code text NOT NULL,
    completed boolean NOT NULL DEFAULT false,
    completed_by uuid REFERENCES app_user (id),
    completed_at timestamptz,
    UNIQUE (application_id, code)
);

CREATE TABLE spring_session (
    primary_id char(36) NOT NULL,
    session_id char(36) NOT NULL,
    creation_time bigint NOT NULL,
    last_access_time bigint NOT NULL,
    max_inactive_interval int NOT NULL,
    expiry_time bigint NOT NULL,
    principal_name varchar(100),
    CONSTRAINT spring_session_pk PRIMARY KEY (primary_id)
);

CREATE UNIQUE INDEX spring_session_ix1 ON spring_session (session_id);
CREATE INDEX spring_session_ix2 ON spring_session (expiry_time);
CREATE INDEX spring_session_ix3 ON spring_session (principal_name);

CREATE TABLE spring_session_attributes (
    session_primary_id char(36) NOT NULL,
    attribute_name varchar(200) NOT NULL,
    attribute_bytes bytea NOT NULL,
    CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_primary_id, attribute_name),
    CONSTRAINT spring_session_attributes_fk
        FOREIGN KEY (session_primary_id) REFERENCES spring_session (primary_id) ON DELETE CASCADE
);

CREATE INDEX application_register_idx ON application (status, cemetery_id, venue_id, created_at DESC);
CREATE INDEX reservation_range_idx ON reservation (venue_id, starts_at, ends_at);
CREATE INDEX integration_attempt_adapter_idx ON integration_attempt (adapter, created_at DESC);
CREATE INDEX audit_event_entity_idx ON audit_event (entity_type, entity_id, occurred_at DESC);

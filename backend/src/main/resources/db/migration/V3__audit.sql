CREATE TABLE audit_event (
    id             uuid         PRIMARY KEY,
    occurred_at    timestamptz  NOT NULL,
    actor_user_id  uuid         REFERENCES app_user (id) ON DELETE SET NULL,
    actor_username varchar(64),
    action         varchar(60)  NOT NULL,
    target_type    varchar(60),
    target_id      varchar(100),
    outcome        varchar(10)  NOT NULL,
    details        jsonb,
    client_ip      varchar(45),
    CONSTRAINT ck_audit_outcome CHECK (outcome IN ('SUCCESS', 'FAILURE', 'DENIED'))
);

CREATE INDEX ix_audit_event_occurred ON audit_event (occurred_at DESC);
CREATE INDEX ix_audit_event_actor ON audit_event (actor_user_id, occurred_at DESC);
CREATE INDEX ix_audit_event_action ON audit_event (action);

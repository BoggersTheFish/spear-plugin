-- TS-Spear canonical reference DDL (schema v1).
-- Production Flyway migrations are dialect-specific in the implementation repo
-- (storage-sqlite/, storage-jdbc/). CHECK constraints use syntax supported by
-- SQLite 3.37+, PostgreSQL, and MySQL 8.0.16+.

CREATE TABLE IF NOT EXISTS tsspear_meta (
    key_name    VARCHAR(64) PRIMARY KEY,
    value_text  TEXT NOT NULL,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO tsspear_meta (key_name, value_text)
VALUES ('schema_version', '1')
ON CONFLICT (key_name) DO NOTHING;

-- ─────────────────────────────────────────────
-- Player state snapshots (periodic + on quit)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ts_player_state (
    player_id       CHAR(36) NOT NULL,
    version         BIGINT NOT NULL CHECK (version >= 0),
    snapshot_json   TEXT NOT NULL,
    trust_json      TEXT NOT NULL,
    suspicion_json  TEXT NOT NULL,
    captured_at     TIMESTAMP NOT NULL,
    PRIMARY KEY (player_id, version)
);

CREATE INDEX IF NOT EXISTS idx_player_state_latest
    ON ts_player_state (player_id, captured_at DESC);

-- ─────────────────────────────────────────────
-- Evidence (persisted above severity threshold)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ts_evidence (
    evidence_id     CHAR(36) PRIMARY KEY,
    player_id       CHAR(36) NOT NULL,
    kind            VARCHAR(64) NOT NULL,
    dimension       VARCHAR(32) NOT NULL,
    severity        DOUBLE NOT NULL CHECK (severity >= 0.0 AND severity <= 1.0),
    credibility     DOUBLE NOT NULL CHECK (credibility >= 0.0 AND credibility <= 1.0),
    weight          DOUBLE NOT NULL CHECK (weight >= 0.0 AND weight <= 1.0),
    timestamp       TIMESTAMP NOT NULL,
    tick            BIGINT,
    world           VARCHAR(64),
    loc_x           DOUBLE,
    loc_y           DOUBLE,
    loc_z           DOUBLE,
    reason          TEXT NOT NULL,
    source_check_id VARCHAR(64) NOT NULL,
    payload_json    TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_evidence_player_time
    ON ts_evidence (player_id, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_evidence_dimension
    ON ts_evidence (player_id, dimension, timestamp DESC);

-- ─────────────────────────────────────────────
-- Evidence relationships
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ts_evidence_link (
    from_evidence_id CHAR(36) NOT NULL,
    to_evidence_id   CHAR(36) NOT NULL,
    link_kind        VARCHAR(32) NOT NULL,
    strength         DOUBLE NOT NULL DEFAULT 1.0 CHECK (strength >= 0.0 AND strength <= 1.0),
    PRIMARY KEY (from_evidence_id, to_evidence_id, link_kind),
    FOREIGN KEY (from_evidence_id) REFERENCES ts_evidence(evidence_id),
    FOREIGN KEY (to_evidence_id) REFERENCES ts_evidence(evidence_id)
);

-- ─────────────────────────────────────────────
-- Confidence receipts (always persisted)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ts_receipt (
    receipt_id          CHAR(36) PRIMARY KEY,
    player_id           CHAR(36) NOT NULL,
    event_time          TIMESTAMP NOT NULL,
    world               VARCHAR(64),
    loc_x               DOUBLE,
    loc_y               DOUBLE,
    loc_z               DOUBLE,
    event_name          VARCHAR(64) NOT NULL,
    dimension           VARCHAR(32) NOT NULL,
    confidence_delta    DOUBLE NOT NULL,
    previous_value      DOUBLE NOT NULL CHECK (previous_value >= 0.0 AND previous_value <= 1.0),
    new_value           DOUBLE NOT NULL CHECK (new_value >= 0.0 AND new_value <= 1.0),
    reason              TEXT NOT NULL,
    state_version       BIGINT NOT NULL CHECK (state_version >= 0),
    tension_before      DOUBLE,
    tension_after       DOUBLE,
    supporting_count    INT CHECK (supporting_count IS NULL OR supporting_count >= 0),
    conflicting_count   INT CHECK (conflicting_count IS NULL OR conflicting_count >= 0),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_receipt_player_time
    ON ts_receipt (player_id, event_time DESC);

CREATE INDEX IF NOT EXISTS idx_receipt_dimension
    ON ts_receipt (player_id, dimension, event_time DESC);

CREATE TABLE IF NOT EXISTS ts_receipt_evidence (
    receipt_id   CHAR(36) NOT NULL,
    evidence_id  CHAR(36) NOT NULL,
    PRIMARY KEY (receipt_id, evidence_id),
    FOREIGN KEY (receipt_id) REFERENCES ts_receipt(receipt_id),
    FOREIGN KEY (evidence_id) REFERENCES ts_evidence(evidence_id)
);

-- ─────────────────────────────────────────────
-- Graph snapshots (optional, for audit)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ts_graph_snapshot (
    snapshot_id     CHAR(36) PRIMARY KEY,
    player_id       CHAR(36) NOT NULL,
    graph_kind      VARCHAR(32) NOT NULL,
    snapshot_json   TEXT NOT NULL,
    captured_at     TIMESTAMP NOT NULL,
    state_version   BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_graph_player
    ON ts_graph_snapshot (player_id, graph_kind, captured_at DESC);

-- ─────────────────────────────────────────────
-- Replay sequences
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ts_replay (
    sequence_id     CHAR(36) PRIMARY KEY,
    player_id       CHAR(36) NOT NULL,
    player_name     VARCHAR(16) NOT NULL,
    start_tick      BIGINT NOT NULL,
    end_tick        BIGINT NOT NULL,
    start_time      TIMESTAMP NOT NULL,
    trigger_reason  VARCHAR(32) NOT NULL,
    frame_count     INT NOT NULL CHECK (frame_count >= 0),
    binary_blob     BLOB,
    json_export     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_replay_player
    ON ts_replay (player_id, start_time DESC);

-- ─────────────────────────────────────────────
-- Staff actions (audit trail)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ts_staff_action (
    action_id       CHAR(36) PRIMARY KEY,
    staff_uuid      CHAR(36) NOT NULL,
    staff_name      VARCHAR(16) NOT NULL,
    target_uuid     CHAR(36) NOT NULL,
    action_type     VARCHAR(32) NOT NULL,
    details_json    TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_staff_target
    ON ts_staff_action (target_uuid, created_at DESC);

-- ─────────────────────────────────────────────
-- ML export batches
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ts_export (
    export_id       CHAR(36) PRIMARY KEY,
    player_id       CHAR(36) NOT NULL,
    exported_by     CHAR(36),
    export_json     TEXT NOT NULL,
    label_json      TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
-- Alerts (notification history)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ts_alert (
    alert_id        CHAR(36) PRIMARY KEY,
    player_id       CHAR(36) NOT NULL,
    dimension       VARCHAR(32) NOT NULL,
    confidence      DOUBLE NOT NULL CHECK (confidence >= 0.0 AND confidence <= 1.0),
    previous_risk   DOUBLE NOT NULL CHECK (previous_risk >= 0.0 AND previous_risk <= 1.0),
    current_risk    DOUBLE NOT NULL CHECK (current_risk >= 0.0 AND current_risk <= 1.0),
    delta           DOUBLE NOT NULL,
    trend           VARCHAR(16) NOT NULL,
    evidence_count  INT NOT NULL CHECK (evidence_count >= 0),
    notified_staff  TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_alert_player
    ON ts_alert (player_id, created_at DESC);
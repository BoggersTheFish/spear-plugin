CREATE TABLE IF NOT EXISTS tsspear_meta (
    key_name    VARCHAR(64) PRIMARY KEY,
    value_text  TEXT NOT NULL,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO tsspear_meta (key_name, value_text)
VALUES ('schema_version', '1')
ON CONFLICT (key_name) DO NOTHING;

CREATE TABLE IF NOT EXISTS ts_player_state (
    player_id       CHAR(36) NOT NULL,
    version         BIGINT NOT NULL,
    snapshot_json   TEXT NOT NULL,
    trust_json      TEXT NOT NULL,
    suspicion_json  TEXT NOT NULL,
    captured_at     TIMESTAMP NOT NULL,
    PRIMARY KEY (player_id, version)
);

CREATE INDEX IF NOT EXISTS idx_player_state_latest
    ON ts_player_state (player_id, captured_at DESC);

CREATE TABLE IF NOT EXISTS ts_evidence (
    evidence_id     CHAR(36) PRIMARY KEY,
    player_id       CHAR(36) NOT NULL,
    kind            VARCHAR(64) NOT NULL,
    dimension       VARCHAR(32) NOT NULL,
    severity        DOUBLE PRECISION NOT NULL,
    credibility     DOUBLE PRECISION NOT NULL,
    weight          DOUBLE PRECISION NOT NULL,
    timestamp       TEXT NOT NULL,
    tick            BIGINT,
    world           VARCHAR(64),
    loc_x           DOUBLE PRECISION,
    loc_y           DOUBLE PRECISION,
    loc_z           DOUBLE PRECISION,
    reason          TEXT NOT NULL,
    source_check_id VARCHAR(64) NOT NULL,
    payload_json    TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_evidence_player_time
    ON ts_evidence (player_id, timestamp DESC);

CREATE TABLE IF NOT EXISTS ts_receipt (
    receipt_id          CHAR(36) PRIMARY KEY,
    player_id           CHAR(36) NOT NULL,
    event_time          TEXT NOT NULL,
    world               VARCHAR(64),
    loc_x               DOUBLE PRECISION,
    loc_y               DOUBLE PRECISION,
    loc_z               DOUBLE PRECISION,
    event_name          VARCHAR(64) NOT NULL,
    dimension           VARCHAR(32) NOT NULL,
    confidence_delta    DOUBLE PRECISION NOT NULL,
    previous_value      DOUBLE PRECISION NOT NULL,
    new_value           DOUBLE PRECISION NOT NULL,
    reason              TEXT NOT NULL,
    state_version       BIGINT NOT NULL,
    tension_before      DOUBLE PRECISION,
    tension_after       DOUBLE PRECISION,
    supporting_count    INT,
    conflicting_count   INT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_receipt_player_time
    ON ts_receipt (player_id, event_time DESC);

CREATE TABLE IF NOT EXISTS ts_receipt_evidence (
    receipt_id   CHAR(36) NOT NULL,
    evidence_id  CHAR(36) NOT NULL,
    PRIMARY KEY (receipt_id, evidence_id),
    FOREIGN KEY (receipt_id) REFERENCES ts_receipt(receipt_id)
);

CREATE TABLE IF NOT EXISTS ts_replay (
    sequence_id     CHAR(36) PRIMARY KEY,
    player_id       CHAR(36) NOT NULL,
    player_name     VARCHAR(16) NOT NULL,
    start_tick      BIGINT NOT NULL,
    end_tick        BIGINT NOT NULL,
    start_time      TEXT NOT NULL,
    trigger_reason  VARCHAR(32) NOT NULL,
    frame_count     INT NOT NULL,
    json_export     TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_replay_player
    ON ts_replay (player_id, start_time DESC);
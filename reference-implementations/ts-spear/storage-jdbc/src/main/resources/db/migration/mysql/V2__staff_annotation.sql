UPDATE tsspear_meta SET value_text = '2', updated_at = CURRENT_TIMESTAMP
WHERE key_name = 'schema_version';

CREATE TABLE IF NOT EXISTS ts_staff_annotation (
    annotation_id    CHAR(36) PRIMARY KEY,
    player_id        CHAR(36) NOT NULL,
    staff_name       VARCHAR(64) NOT NULL,
    verdict          VARCHAR(32) NOT NULL,
    notes            TEXT,
    annotated_at     TEXT NOT NULL,
    linked_export_id CHAR(36),
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_annotation_player_time
    ON ts_staff_annotation (player_id, annotated_at DESC);
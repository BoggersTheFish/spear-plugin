package com.badgersmc.tsspear.storage.jdbc;

public enum SqlDialect {
    MYSQL("classpath:db/migration/mysql"),
    POSTGRESQL("classpath:db/migration/postgresql");

    private final String migrationLocation;

    SqlDialect(String migrationLocation) {
        this.migrationLocation = migrationLocation;
    }

    public String migrationLocation() {
        return migrationLocation;
    }

    public String insertIgnoreEvidence() {
        return switch (this) {
            case MYSQL -> "INSERT IGNORE INTO ts_evidence";
            case POSTGRESQL -> "INSERT INTO ts_evidence";
        };
    }

    public String insertIgnoreReceiptEvidence() {
        return switch (this) {
            case MYSQL -> "INSERT IGNORE INTO ts_receipt_evidence (receipt_id, evidence_id) VALUES (?, ?)";
            case POSTGRESQL ->
                "INSERT INTO ts_receipt_evidence (receipt_id, evidence_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        };
    }

    public String evidenceConflictClause() {
        return switch (this) {
            case MYSQL -> "";
            case POSTGRESQL -> " ON CONFLICT (evidence_id) DO NOTHING";
        };
    }
}
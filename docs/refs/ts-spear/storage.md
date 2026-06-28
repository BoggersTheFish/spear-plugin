# Storage

TS-Spear uses Flyway migrations for schema evolution. Current schema version: **2**.

## Backends

| Backend | Config Value | Module |
|---------|--------------|--------|
| SQLite (default) | `SQLITE` | `storage-sqlite` |
| MySQL | `MYSQL` | `storage-jdbc` |
| PostgreSQL | `POSTGRESQL` | `storage-jdbc` |
| In-memory (dev) | `MEMORY` | `core` in-memory stores |

## SQLite (Default)

Database file: `plugins/TSSpear/tsspear.db`

Migrations: `storage-sqlite/src/main/resources/db/migration/`

- **V1** — `ts_player_state`, `ts_evidence`, `ts_receipt`, `ts_replay`
- **V2** — `ts_staff_annotation` (ML training labels)

## MySQL / PostgreSQL

```yaml
storage:
  backend: MYSQL   # or POSTGRESQL
  mysql:
    host: localhost
    port: 3306
    database: tsspear
    username: root
    password: ""
  postgres:
    host: localhost
    port: 5432
    database: tsspear
    username: postgres
    password: ""
```

Migrations are dialect-specific under `storage-jdbc/src/main/resources/db/migration/{mysql,postgresql}/`.

## Tables

| Table | Purpose |
|-------|---------|
| `ts_player_state` | Versioned PlayerState snapshots |
| `ts_evidence` | Persisted evidence above weight threshold |
| `ts_receipt` | Confidence mutation audit trail |
| `ts_receipt_evidence` | Receipt ↔ evidence linkage |
| `ts_replay` | Stored replay sequences (JSON export) |
| `ts_staff_annotation` | Staff ML training labels |

Canonical DDL: [schemas/database.sql](./schemas/database.sql)

## Write-Behind Pattern

`StorageEngine` batches receipts and evidence on a worker queue:

- `storage.batch-size` — flush batch size (default 100)
- `storage.flush-interval-ms` — periodic flush (default 50ms)
- `storage.evidence-persist-threshold` — minimum weight to persist (default 0.25)

Main thread never blocks on I/O. Monitor queue depth via `/ts debug profile`.

## Ports

| Port | Implementations |
|------|-----------------|
| `PlayerStateRepository` | Sqlite, Jdbc, InMemory |
| `EvidenceStore` | Sqlite, Jdbc, InMemory |
| `ReceiptStore` | Sqlite, Jdbc, InMemory |
| `ReplayStore` | Sqlite, Jdbc, InMemory |
| `AnnotationStore` | Sqlite, Jdbc, InMemory |

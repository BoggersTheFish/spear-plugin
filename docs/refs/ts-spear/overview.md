# TS-Spear — SPEAR Reference Implementation

**Captured:** 2026-06-28  
**Source:** [`reference-implementations/ts-spear/`](../../reference-implementations/ts-spear/)  
**Status:** Phases 0–5 implemented · Paper 1.21+ · Java 21

## What This Is

**TS-Spear** is a domain-specific reference implementation of SPEAR architectural discipline applied to a real-time intelligence system — a Paper Minecraft anti-cheat that emits evidence instead of silent bans.

It is **not** a fork of this repository (`spear-plugin`). This repo packages the SPEAR *methodology* for Claude Code. TS-Spear packages the SPEAR *patterns* into a production JVM system.

## Why It Lives in `docs/refs/`

Following the [`hook-schema.md`](../hook-schema.md) pattern, this overview is a **citeable entry point** for teams using SPEAR to build complex systems. Full documentation lives alongside the source under `reference-implementations/ts-spear/docs/`.

## SPEAR Pattern Mapping

| SPEAR (this repo) | TS-Spear (reference impl) |
|-------------------|---------------------------|
| `spec → prove → engine → arch → refine` | Phase 0–5 delivery gates with tests per module |
| EARS `REQ-*` IDs | Check IDs + evidence kinds + confidence dimensions |
| `docs/requirements.md` | Architecture doc §11–12 |
| Konsist layer rules | `domain` → `application` → `infrastructure` (zero Bukkit in domain) |
| `.claude/spear-state.json` | `PlayerState` version counter + `ConfidenceReceipt` audit trail |
| Evidence blocks before progression | Checks return `Evidence`, never `ban()` |
| Flyway / schema migrations | Flyway V1 (core tables) + V2 (`ts_staff_annotation`) |
| Hook session-start injection | `PlayerJoinHook` seeds state; `EventBus` propagates samples |
| `prove` = failing test first | Per-check unit tests + stress harness (500 players) |
| `arch` = layer scan | Hexagonal module boundaries in Gradle layout |

## North Stars (TS Philosophy)

1. **Nothing is boolean** — multi-dimensional confidence with decay and explanation.
2. **Checks emit evidence, not punishments** — staff own enforcement.
3. **Everything is explainable** — scores decompose into supporting/conflicting evidence.
4. **Everything produces receipts** — `ConfidenceReceipt` on every mutation.
5. **Everything is observable** — `/ts debug profile` for 500+ player hot paths.
6. **Everything is replayable** — ring buffer + continuous capture on alert.
7. **AI-ready by contract** — `InferenceProvider` SPI, schema v1 JSON/JSONL export.

## Module Layout

```
api/  core/  storage-sqlite/  storage-jdbc/
checks/movement/  checks/combat/  checks/networking/
benchmarks/  plugin/
```

## Phase Delivery (Summary)

| Phase | Delivered |
|-------|-----------|
| 0–1 | Gradle scaffold, movement checks, SQLite, decay, replay |
| 2 | Combat checks, 5 graph types, GUI suite, JSON export |
| 3 | Freeze, JDBC backends, i18n, check management GUI |
| 4 | Object pooling, packet checks, JMH benchmarks, stress tests |
| 5 | ML export, staff annotations, `InferenceProvider` SPI |

## Build & Verify

```bash
cd reference-implementations/ts-spear
./gradlew build
# plugin/build/libs/TSSpear-0.1.0-SNAPSHOT.jar
```

## Documentation

All docs, schemas, and PlantUML diagrams:

**[`reference-implementations/ts-spear/docs/README.md`](../../reference-implementations/ts-spear/docs/README.md)**

| Doc | Purpose |
|-----|---------|
| [Architecture](../../reference-implementations/ts-spear/docs/architecture/TS-SPEAR-ARCHITECTURE.md) | Full system design |
| [Core Interfaces](../../reference-implementations/ts-spear/docs/architecture/CORE-INTERFACES.md) | Port and engine contracts |
| [Phases](../../reference-implementations/ts-spear/docs/PHASES.md) | Phase 0–5 status + roadmap |
| [Installation](../../reference-implementations/ts-spear/docs/INSTALLATION.md) | Paper deployment |
| [Configuration](../../reference-implementations/ts-spear/docs/CONFIGURATION.md) | `config.yml` reference |
| [Commands](../../reference-implementations/ts-spear/docs/COMMANDS.md) | Staff `/ts` commands |
| [Storage](../../reference-implementations/ts-spear/docs/STORAGE.md) | SQLite / MySQL / PostgreSQL |
| [Development](../../reference-implementations/ts-spear/docs/DEVELOPMENT.md) | Contributor build guide |
| [ML Hooks](../../reference-implementations/ts-spear/docs/ML-HOOKS.md) | ML export + inference SPI |
| [GUI Wireframes](../../reference-implementations/ts-spear/docs/wireframes/GUI-WIREFRAMES.md) | Staff GUI designs |
| [Schemas](../../reference-implementations/ts-spear/docs/schemas/) | JSON schemas + DDL |
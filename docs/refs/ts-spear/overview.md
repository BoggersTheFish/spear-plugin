# TS-Spear — SPEAR Reference Implementation

**Captured:** 2026-06-28  
**Implementation source:** [`reference-implementations/ts-spear/`](../../reference-implementations/ts-spear/) (in this repository)  
**Status:** Phases 0–5 implemented · Paper 1.21+ · Java 21

## What This Is

**TS-Spear** is a domain-specific reference implementation of SPEAR architectural discipline applied to a real-time intelligence system — a Paper Minecraft anti-cheat that emits evidence instead of silent bans.

It is **not** a fork of this repository (`spear-plugin`). This repo packages the SPEAR *methodology* for Claude Code. TS-Spear packages the SPEAR *patterns* into a production JVM system.

## Why It Lives in `docs/refs/`

Following the `docs/refs/hook-schema.md` pattern, this directory captures an **exhaustive, citeable reference** for teams using SPEAR to build complex systems. Use it when:

- Bootstrapping a SPEAR JVM project with hexagonal layering
- Demonstrating evidence-gated progression in a non-Claude-Code domain
- Studying how SPEAR receipts, migrations, and TDD map to runtime systems
- Integrating ML export pipelines behind stable ports

## SPEAR Pattern Mapping

| SPEAR (this repo) | TS-Spear (reference impl) |
|-------------------|---------------------------|
| `spec → prove → engine → arch → refine` | Phase 0–5 delivery gates with tests per module |
| EARS `REQ-*` IDs | Check IDs + evidence kinds + confidence dimensions |
| `docs/requirements.md` | `docs/refs/ts-spear/architecture.md` §11–12 |
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

Full detail: [phases.md](./phases.md)

## Build & Verify

```bash
cd reference-implementations/ts-spear
./gradlew build
# plugin/build/libs/TSSpear-0.1.0-SNAPSHOT.jar
```

## Document Index

| Doc | Purpose |
|-----|---------|
| [README.md](./README.md) | Local index |
| [architecture.md](./architecture.md) | Full 1,300-line system design |
| [core-interfaces.md](./core-interfaces.md) | Port and engine contracts |
| [phases.md](./phases.md) | Phase 0–5 status + roadmap |
| [installation.md](./installation.md) | Paper deployment |
| [configuration.md](./configuration.md) | `config.yml` reference |
| [commands.md](./commands.md) | Staff `/ts` commands |
| [storage.md](./storage.md) | SQLite / MySQL / PostgreSQL |
| [development.md](./development.md) | Contributor build guide |
| [ml-hooks.md](./ml-hooks.md) | ML export + inference SPI |
| [gui-wireframes.md](./gui-wireframes.md) | Staff GUI designs |
| [schemas/](./schemas/) | JSON schemas + DDL |

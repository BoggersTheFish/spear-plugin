# TS-Spear Documentation Index

Complete documentation for the TS-Spear Thinking System anti-cheat intelligence engine.

## Quick Start

| Document | Audience | Description |
|----------|----------|-------------|
| [Installation](./INSTALLATION.md) | Server owners | Deploy on Paper 1.21+ |
| [Configuration](./CONFIGURATION.md) | Operators | All `config.yml` keys |
| [Commands](./COMMANDS.md) | Staff | `/ts` command reference |
| [Development](./DEVELOPMENT.md) | Contributors | Local build, modules, testing |

## Architecture

| Document | Description |
|----------|-------------|
| [Architecture Design](./architecture/TS-SPEAR-ARCHITECTURE.md) | Full system design (1,300+ lines) |
| [Core Interfaces](./architecture/CORE-INTERFACES.md) | Port and engine contracts |
| [GUI Wireframes](./wireframes/GUI-WIREFRAMES.md) | Staff interface designs |
| [PlantUML Diagrams](./architecture/diagrams/) | Sequence, component, state machine diagrams |

## Data & Schemas

| Document | Description |
|----------|-------------|
| [Storage](./STORAGE.md) | SQLite, MySQL, PostgreSQL backends |
| [Database DDL](./schemas/database.sql) | Canonical schema |
| [Evidence Schema](./schemas/evidence.schema.json) | Evidence JSON format |
| [Receipt Schema](./schemas/receipt.schema.json) | Confidence receipt format |
| [Replay Schema](./schemas/replay.schema.json) | Replay sequence format |
| [Export Schema](./schemas/export.schema.json) | ML export batch (schema v1) |

## Machine Learning

| Document | Description |
|----------|-------------|
| [ML Hooks](./ML-HOOKS.md) | Feature export, inference SPI, external models |

## Project Status

| Document | Description |
|----------|-------------|
| [Implementation Phases](./PHASES.md) | Phase 0–5 delivery status and roadmap |

## Philosophy

TS-Spear is part of the [BoggersTheFish Thinking System (TS)](https://www.boggersthefish.com/ts-os) ecosystem and adopts architectural discipline from [BadgersMC/spear-plugin](https://github.com/BadgersMC/spear-plugin) (SPEAR methodology).

### North Stars

1. **Nothing is boolean** — confidence is multi-dimensional and continuous.
2. **Checks emit evidence, not punishments** — staff own enforcement.
3. **Everything is explainable** — any score decomposes into supporting and conflicting evidence.
4. **Everything produces receipts** — immutable audit trail for confidence mutations.
5. **Everything is observable** — metrics, traces, live inspection for 500+ players.
6. **Everything is replayable** — suspicious sequences reconstruct tick-by-tick.
7. **AI-ready by contract** — stable export interfaces without architectural churn.
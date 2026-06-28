# TS-Spear — Architecture Design Document

**Version:** 0.1.0-draft  
**Date:** 2026-06-28  
**Status:** Implemented (Phases 0–5) — see [phases.md](./phases.md)  
**Owner:** BadgersMC / BoggersTheFish TS Ecosystem  
**Reference studied:** [BadgersMC/spear-plugin](https://github.com/BadgersMC/spear-plugin)

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Reference Analysis — spear-plugin Reverse Engineering](#2-reference-analysis--spear-plugin-reverse-engineering)
3. [TS Philosophy Mapping](#3-ts-philosophy-mapping)
4. [System Overview](#4-system-overview)
5. [Package Structure](#5-package-structure)
6. [Class Hierarchy](#6-class-hierarchy)
7. [Data Model](#7-data-model)
8. [Graph Model](#8-graph-model)
9. [Event Flow](#9-event-flow)
10. [Plugin Lifecycle](#10-plugin-lifecycle)
11. [Engine Specifications](#11-engine-specifications)
12. [Check Framework](#12-check-framework)
13. [Machine Learning Interfaces](#13-machine-learning-interfaces)
14. [Staff Tools](#14-staff-tools)
15. [Storage & Persistence](#15-storage--persistence)
16. [Configuration](#16-configuration)
17. [Performance Architecture](#17-performance-architecture)
18. [Developer Experience](#18-developer-experience)
19. [Testing Strategy](#19-testing-strategy)
20. [Gradle Project Layout](#20-gradle-project-layout)
21. [GUI Wireframes](#21-gui-wireframes)
22. [Format Specifications](#22-format-specifications)
23. [Roadmap — MVP to Enterprise](#23-roadmap--mvp-to-enterprise)
24. [Key Decisions](#24-key-decisions)
25. [PR Plan](#25-pr-plan)

**Appendices**

- [B: JSON Schemas](./schemas/)
- [C: Database DDL](./schemas/database.sql)

---

## 1. Executive Summary

**TS-Spear** is a Paper Minecraft plugin that reframes anti-cheat from a collection of boolean flags into a **state-driven intelligence engine**. Every player action produces **evidence**; evidence propagates through typed **graphs**; graphs create **tension**; tension updates **confidence**; confidence drives **staff recommendations** — never automatic punishment by default.

The name deliberately bridges two lineages:

| Lineage | Contribution |
|---------|--------------|
| **SPEAR** (Spec-Proven Engineering with Architectural Requirements) | Hexagonal layering, evidence-gated progression, state machines, receipts, migrations, TDD-first module design |
| **TS** (Thinking System) | Graph-native reasoning, tension/confidence dynamics, verifiable receipts, AI-consumable traces, explainability-first |

TS-Spear is **not** a fork or port of spear-plugin. The reference repository is a Claude Code methodology plugin, not a Minecraft anti-cheat. We extract its **architectural discipline** and design a domain-specific intelligence platform from first principles.

### Design North Stars

1. **Nothing is boolean** — every dimension carries confidence, evidence, decay, timestamp, and explanation.
2. **Checks emit evidence, not punishments** — the Evidence Engine owns confidence; staff own enforcement.
3. **Everything is explainable** — any suspicion score decomposes into supporting and conflicting evidence with net confidence.
4. **Everything produces receipts** — immutable audit records for every confidence mutation.
5. **Everything is observable** — metrics, traces, and live inspection for 500+ concurrent players.
6. **Everything is replayable** — suspicious sequences can be reconstructed tick-by-tick.
7. **AI-ready by contract** — stable export interfaces for future models without architectural churn.

---

## 2. Reference Analysis — spear-plugin Reverse Engineering

### 2.1 What spear-plugin Actually Is

After full repository inspection, `BadgersMC/spear-plugin` is a **Claude Code plugin** packaging the SPEAR software engineering methodology. It contains:

- Seven workflow skills (`spec`, `prove`, `engine`, `arch`, `refine`, `init`, `using-spear`)
- Session-start hooks injecting context into AI sessions
- A per-project state file (`.claude/spear-state.json`)
- Layer dependency enforcement via Konsist
- Evidence blocks required before code progression
- Bash/Node test infrastructure for hooks and state machines

**It is not a Minecraft plugin. It does not contain movement checks, packet analysis, or anti-cheat logic.**

### 2.2 Architectural Patterns Worth Adopting

| SPEAR Pattern | Why It Exists | TS-Spear Adaptation |
|---------------|---------------|---------------------|
| **Linear phase state machine** | Prevents skipping verification steps | Player trust lifecycle: `OBSERVE → ACCUMULATE → ELEVATE → REVIEW → RESOLVE` |
| **Evidence blocks on tasks** | "Verify don't guess" — no progression without cited sources | Every check must cite `EvidenceSource` (packet, sample, replay frame) |
| **Receipts / audit trail** | Mechanical proof of what changed and why | `ConfidenceReceipt` on every graph mutation |
| **Hexagonal layers** | Domain purity; framework at edges | `domain` has zero Bukkit imports; `infrastructure` owns Paper |
| **Hooks at session start** | Inject context before work begins | `PlayerJoinHook` seeds `PlayerState`; `TickStartHook` schedules async work |
| **Atomic state writes** | Prevent corruption during concurrent updates | Copy-on-write `PlayerState` snapshots with version counters |
| **Schema migrations** | Safe evolution of persisted state | Flyway migrations for DB; versioned replay/evidence schemas |
| **Checks return tests, not features** | TDD: prove before implement | Checks return `Evidence`, never `ban()` |
| **Orchestrator/worker split** | Heavy judgment vs mechanical work | Main thread captures samples; worker pool runs checks |
| **Truncation priority** | Bounded context under load | Alert payloads truncate history before dropping confidence breakdown |

### 2.3 SPEAR Strengths

1. **Enforced discipline** — gates are mechanical, not advisory.
2. **Explainable progression** — phase names and REQ-IDs make state legible.
3. **Composable with other systems** — explicit deferral boundaries (superpowers composition).
4. **Testable infrastructure** — hooks and state machines have fixture-based tests.
5. **Cross-platform hook design** — polyglot dispatch pattern.

### 2.4 SPEAR Limitations (for our domain)

1. **No real-time stream processing** — batch/session model, not 20 TPS event streams.
2. **No graph propagation** — state is scalar phase, not multi-dimensional confidence graphs.
3. **No temporal decay** — state persists until cleared; no evidence aging.
4. **No spatial reasoning** — irrelevant to Claude Code, essential for movement AC.
5. **Human-in-the-loop only** — no sub-100ms automated inference path.
6. **Single-actor state** — one task at a time; we need per-player concurrent graphs.

### 2.5 Conventional Anti-Cheat Patterns TS-Spear Replaces

| Legacy Pattern | Problem | TS-Spear Replacement |
|----------------|---------|----------------------|
| `if (speed > max) flag++` | No context, no decay, no explanation | `ImpossibleMovementCheck` → `Evidence` → graph propagation → confidence delta |
| Violation counters | Linear accumulation ignores contradictions | Tension graph with supporting/contradicting edges |
| Silent autoban | Staff cannot audit decision | Recommendation engine + receipt chain |
| Per-check silos | Combat and movement don't inform each other | Cross-domain `ConstraintGraph` dependencies |
| Config thresholds only | Cannot adapt to server lag | `LatencyState` and `EnvironmentState` as contradiction sources |

---

## 3. TS Philosophy Mapping

TS-Spear implements the BoggersTheFish Thinking System primitives in the Minecraft domain:

| TS Primitive | Minecraft Domain Mapping |
|--------------|--------------------------|
| **Node** | `GraphNode` — Movement, Combat, Ping, etc. |
| **Edge** | `GraphEdge` — supports, contradicts, requires, implies, boosts, reduces |
| **Activation** | Current signal strength of a suspicion dimension |
| **Tension** | Unresolved conflict between supporting and contradicting evidence |
| **Stability** | Confidence after decay and contradiction resolution |
| **Coherence** | Player state that holds without internal contradiction |
| **Receipt** | `ConfidenceReceipt` — verifiable mutation record |
| **Wave / Propagate** | Evidence propagation pass through graphs |

### Core Loop (TS-native)

```text
Event → Sample → Evidence → Graph Insert → Tension Compute → Confidence Update → Receipt → Notify
         ↑                                                                              ↓
         └──────────────────────── Decay Timer ←──────────────────────────────────────┘
```

### Confidence Dimensions (never boolean)

```text
TrustState
├── Movement      { confidence, evidence[], decay, timestamp, explanation }
├── Combat        { confidence, evidence[], decay, timestamp, explanation }
├── Inventory     { confidence, evidence[], decay, timestamp, explanation }
├── Automation    { confidence, evidence[], decay, timestamp, explanation }
├── Networking    { confidence, evidence[], decay, timestamp, explanation }
└── Interaction   { confidence, evidence[], decay, timestamp, explanation }
```

---

## 4. System Overview

### 4.1 Layered Architecture (Hexagonal)

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                        infrastructure/                                   │
│  Paper listeners, commands, GUIs, storage adapters, metrics, packet hooks │
├─────────────────────────────────────────────────────────────────────────┤
│                        application/                                      │
│  Services, engines, event bus, schedulers, notification dispatchers     │
├─────────────────────────────────────────────────────────────────────────┤
│                        domain/                                           │
│  PlayerState, Evidence, Graphs, Receipts, Checks (pure logic)           │
└─────────────────────────────────────────────────────────────────────────┘
```

**Dependency rule:** `domain` → nothing. `application` → `domain`. `infrastructure` → `application` + `domain`.

Enforced by Konsist `LayerRulesTest` (adapted from spear-plugin template).

### 4.2 Engine Topology

```text
                    ┌──────────────┐
                    │  Event Bus   │
                    └──────┬───────┘
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
    ┌────────────┐  ┌────────────┐  ┌────────────┐
    │  Capture   │  │  Command   │  │  Packet    │
    │  Adapters  │  │  Engine    │  │  Bridge    │
    └─────┬──────┘  └────────────┘  └─────┬──────┘
          │                               │
          ▼                               ▼
    ┌─────────────────────────────────────────────┐
    │              Check Engine                    │
    │  (module registry, scheduling, batching)     │
    └─────────────────────┬───────────────────────┘
                          │ Evidence[]
                          ▼
    ┌─────────────────────────────────────────────┐
    │            Evidence Engine                   │
    │  normalize, dedupe, score, link              │
    └─────────────────────┬───────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
    ┌──────────┐   ┌──────────┐   ┌──────────┐
    │  Graph   │   │  State   │   │  Replay  │
    │  Engine  │   │  Engine  │   │  Engine  │
    └────┬─────┘   └────┬─────┘   └────┬─────┘
         │              │              │
         └──────────────┼──────────────┘
                        ▼
              ┌─────────────────┐
              │ Storage Engine  │ (async)
              └────────┬────────┘
                       ▼
              ┌─────────────────┐
              │ Notification    │
              │ + GUI Engine    │
              └─────────────────┘
```

### 4.3 Threading Model

| Thread | Responsibility | Budget |
|--------|----------------|--------|
| **Main (Server)** | Event capture, sample extraction, state read for sync needs | < 0.5ms/player/tick aggregate |
| **TS Worker Pool** | Check execution, evidence normalization, graph propagation | Bounded queue, drop-oldest under pressure |
| **Storage Pool** | DB writes, replay persistence, export jobs | Batched every 50-100ms |
| **Decay Scheduler** | Periodic confidence decay application | Every 1s, staggered by player shard |

**Invariant:** No blocking I/O on the main thread. No `synchronized` on hot paths.

---

## 5. Package Structure

```text
com.badgersmc.tsspear/
├── TSSpearPlugin.java                    # Bootstrap, DI container init
├── api/                                  # Public extension API
│   ├── event/
│   ├── evidence/
│   ├── check/
│   └── export/
├── domain/
│   ├── model/
│   │   ├── player/
│   │   │   ├── PlayerState.java
│   │   │   ├── MovementState.java
│   │   │   ├── CombatState.java
│   │   │   ├── InventoryState.java
│   │   │   ├── InteractionState.java
│   │   │   ├── LatencyState.java
│   │   │   ├── EnvironmentState.java
│   │   │   ├── StaffState.java
│   │   │   ├── BehaviourState.java
│   │   │   ├── TrustState.java
│   │   │   └── SuspicionState.java
│   │   ├── evidence/
│   │   │   ├── Evidence.java
│   │   │   ├── EvidenceId.java
│   │   │   ├── EvidenceKind.java
│   │   │   ├── EvidenceWeight.java
│   │   │   └── EvidenceLink.java
│   │   ├── graph/
│   │   │   ├── GraphNode.java
│   │   │   ├── GraphEdge.java
│   │   │   ├── EdgeKind.java
│   │   │   ├── EvidenceGraph.java
│   │   │   ├── ConstraintGraph.java
│   │   │   ├── TensionGraph.java
│   │   │   ├── RiskGraph.java
│   │   │   └── HistoryGraph.java
│   │   ├── confidence/
│   │   │   ├── ConfidenceVector.java
│   │   │   ├── ConfidenceDimension.java
│   │   │   ├── ConfidenceSnapshot.java
│   │   │   └── DecayCurve.java
│   │   ├── receipt/
│   │   │   ├── ConfidenceReceipt.java
│   │   │   └── ReceiptChain.java
│   │   ├── sample/
│   │   │   ├── MovementSample.java
│   │   │   ├── CombatSample.java
│   │   │   ├── PacketSample.java
│   │   │   └── InteractionSample.java
│   │   └── replay/
│   │       ├── ReplayFrame.java
│   │       └── ReplaySequence.java
│   ├── check/
│   │   ├── Check.java
│   │   ├── CheckContext.java
│   │   ├── CheckResult.java
│   │   └── CheckCategory.java
│   ├── port/                             # Hexagonal ports
│   │   ├── PlayerStateRepository.java
│   │   ├── EvidenceStore.java
│   │   ├── ReplayStore.java
│   │   ├── ReceiptStore.java
│   │   ├── InferenceProvider.java
│   │   ├── PredictionProvider.java
│   │   └── NotificationSink.java
│   └── service/                          # Pure domain services
│       ├── ConfidenceResolver.java
│       ├── TensionCalculator.java
│       ├── DecayApplicator.java
│       └── ExplainabilityService.java
├── application/
│   ├── bus/
│   │   ├── EventBus.java
│   │   ├── TSEvent.java
│   │   └── EventHandler.java
│   ├── engine/
│   │   ├── CheckEngine.java
│   │   ├── EvidenceEngine.java
│   │   ├── GraphEngine.java
│   │   ├── StateEngine.java
│   │   ├── CommandEngine.java
│   │   ├── GuiEngine.java
│   │   ├── NotificationEngine.java
│   │   ├── ReplayEngine.java
│   │   └── StorageEngine.java
│   ├── service/
│   │   ├── PlayerLifecycleService.java
│   │   ├── TrustEvaluationService.java
│   │   ├── AlertService.java
│   │   ├── FreezeService.java
│   │   └── ExportService.java
│   ├── scheduler/
│   │   ├── TickScheduler.java
│   │   ├── DecayScheduler.java
│   │   └── BatchFlushScheduler.java
│   ├── ml/
│   │   ├── FeatureExtractor.java
│   │   ├── SequenceBuilder.java
│   │   ├── EvidenceExporter.java
│   │   └── MlHooks.java
│   └── di/
│       ├── ServiceRegistry.java
│       └── Module.java
├── infrastructure/
│   ├── paper/
│   │   ├── listener/
│   │   ├── packet/
│   │   └── bridge/
│   ├── command/
│   │   └── TSSpearCommand.java
│   ├── gui/
│   │   ├── InspectGui.java
│   │   ├── GraphGui.java
│   │   ├── TimelineGui.java
│   │   └── ReplayGui.java
│   ├── storage/
│   │   ├── sqlite/
│   │   ├── mysql/
│   │   ├── postgres/
│   │   └── json/
│   ├── metrics/
│   │   └── TSSpearMetrics.java
│   └── config/
│       └── TSSpearConfig.java
└── check/                                # Check modules (plugin modules)
    ├── movement/
    ├── combat/
    ├── inventory/
    ├── automation/
    ├── networking/
    └── interaction/
```

---

## 6. Class Hierarchy

### 6.1 Core Type Hierarchy

```text
TSEvent (sealed interface)
├── PlayerSampleEvent
├── EvidenceProducedEvent
├── ConfidenceChangedEvent
├── AlertTriggeredEvent
└── ReplayFrameCapturedEvent

Evidence (record, immutable)
├── fields: id, kind, dimension, weight, timestamp, location, payload, sourceCheck

GraphNode (record)
├── fields: id, kind, activation, confidence, weight, timestamp, reason,
│            supportingEvidence, conflictingEvidence, decayRate, dependencies, receipts

DecayCurve (sealed interface)
├── LinearDecay
├── ExponentialDecay
└── LogarithmicDecay

Check (interface)
└── AbstractCheck (abstract class)
    ├── AbstractMovementCheck
    │   ├── ImpossibleMovementCheck
    │   ├── FlyCheck
    │   ├── SpeedCheck
    │   ├── JesusCheck
    │   └── ScaffoldCheck
    ├── CombatCheck
    │   ├── ReachCheck
    │   ├── KillAuraCheck
    │   ├── AimCheck
    │   └── VelocityCheck
    └── ...

CheckModule (interface) — groups related checks, owns config slice
```

### 6.2 Engine Base Classes

```text
AbstractEngine (lifecycle: start, stop, health)
├── CheckEngine
│   └── manages CheckRegistry, CheckScheduler, CheckContextFactory
├── EvidenceEngine
│   └── normalize → validate → link → emit
├── GraphEngine
│   └── insertEvidence → propagate → computeTension → resolve
├── StateEngine
│   └── load → mutate → snapshot → publish
├── ReplayEngine
│   └── capture → buffer → flush → seek
└── StorageEngine
    └── batch → serialize → write → ack

AbstractRepository<T> (port adapter base)
├── JdbcPlayerStateRepository
├── JdbcEvidenceRepository
└── JsonExportRepository
```

### 6.3 PlayerState Composition

```text
PlayerState (aggregate root)
├── uuid: UUID
├── version: long                    # optimistic concurrency
├── movement: MovementState
├── combat: CombatState
├── inventory: InventoryState
├── interaction: InteractionState
├── latency: LatencyState
├── environment: EnvironmentState
├── staff: StaffState
├── behaviour: BehaviourState
├── trust: TrustState
├── suspicion: SuspicionState
├── evidenceGraph: EvidenceGraph
├── constraintGraph: ConstraintGraph
├── tensionGraph: TensionGraph
├── riskGraph: RiskGraph
├── historyGraph: HistoryGraph
└── lastMutation: Instant
```

---

## 7. Data Model

### 7.1 Evidence

```java
public record Evidence(
    EvidenceId id,
    EvidenceKind kind,              // MOVEMENT_ANOMALY, REACH_EXCESS, PACKET_INVALID, ...
    ConfidenceDimension dimension,
    double severity,                // 0.0 – 1.0 raw signal strength
    double credibility,             // check-specific trust in this reading
    Instant timestamp,
    WorldPosition location,
    String reason,                  // human-readable, never empty
    String sourceCheckId,
    Map<String, Object> payload,    // structured, schema-versioned
    Set<EvidenceId> relatedEvidence
) {}
```

### 7.2 ConfidenceDimension State

```java
public record DimensionConfidence(
    ConfidenceDimension dimension,
    double confidence,              // 0.0 – 1.0 resolved
    double rawActivation,           // pre-decay signal
    double tension,                 // unresolved conflict
    Instant timestamp,
    Instant lastEvidenceAt,
    DecayCurve decayCurve,
    double decayRate,
    String explanation,
    List<EvidenceId> supportingEvidence,
    List<EvidenceId> conflictingEvidence,
    List<ConfidenceReceiptId> receipts
) {}
```

### 7.3 TrustState

```java
public record TrustState(
    ConfidenceVector vector,
    double aggregateRisk,             // derived, not primary
    double netConfidence,             // post-contradiction resolution
    TrendDirection trend,             // RISING, FALLING, STABLE
    double trendDelta,
    Instant evaluatedAt
) {
    // Trust is NOT inverted suspicion — staff overrides and history matter
}
```

### 7.4 ConfidenceReceipt

```java
public record ConfidenceReceipt(
    UUID receiptId,
    UUID playerId,
    Instant time,
    String world,
    WorldPosition location,
    String event,
    ConfidenceDimension dimension,
    double confidenceDelta,
    double previousValue,
    double newValue,
    String reason,
    List<EvidenceId> linkedEvidenceIds,
    long stateVersion
) {}
```

### 7.5 MovementSample (capture primitive)

```java
public record MovementSample(
    UUID playerId,
    long tick,
    Instant timestamp,
    WorldPosition from,
    WorldPosition to,
    Vector velocity,
    boolean onGround,
    boolean inWater,
    boolean inLava,
    boolean gliding,
    boolean sneaking,
    float fallDistance,
    int pingMs,
    GameMode gameMode
) {}
```

---

## 8. Graph Model

### 8.1 Graph Types and Purpose

| Graph | Purpose | Node Examples | Edge Semantics |
|-------|---------|---------------|----------------|
| **EvidenceGraph** | Raw evidence linkage | `EV-001`, `EV-002` | `supports`, `contradicts`, `duplicates` |
| **ConstraintGraph** | World/player constraints | `ON_GROUND`, `LAG_SPIKE`, `IN_CREATIVE` | `requires`, `implies`, `relaxes` |
| **TensionGraph** | Unresolved conflicts | `MOVEMENT_HIGH`, `LAG_EXCUSE` | `supports`, `contradicts` |
| **RiskGraph** | Staff-facing rollup | `MOVEMENT`, `COMBAT` | `boosts`, `reduces` |
| **HistoryGraph** | Temporal patterns | `PATTERN_3x`, `FIRST_OFFENSE` | `implies`, `requires` |

### 8.2 GraphNode Schema

```java
public record GraphNode(
    String nodeId,
    GraphKind graphKind,
    NodeCategory category,          // MOVEMENT, COMBAT, PING, INVENTORY, ...
    double activation,              // current signal 0.0 – 1.0
    double confidence,              // resolved belief 0.0 – 1.0
    double weight,
    Instant timestamp,
    String reason,
    List<EvidenceId> supportingEvidence,
    List<EvidenceId> conflictingEvidence,
    double decayRate,
    Set<String> dependencies,       // node IDs
    List<UUID> receipts
) {}
```

### 8.3 GraphEdge Schema

```java
public record GraphEdge(
    String edgeId,
    String fromNodeId,
    String toNodeId,
    EdgeKind kind,                  // SUPPORTS, CONTRADICTS, REQUIRES, IMPLIES, BOOSTS, REDUCES
    double strength,                // 0.0 – 1.0
    Instant createdAt,
    Optional<Instant> expiresAt,
    String reason
) {}

public enum EdgeKind {
    SUPPORTS, CONTRADICTS, REQUIRES, IMPLIES, BOOSTS, REDUCES
}
```

### 8.4 Propagation Algorithm (GraphEngine)

```text
1. INSERT evidence node into EvidenceGraph
2. MATCH constraint templates → insert/update ConstraintGraph nodes
3. For each new edge E:
   a. If E.kind == SUPPORTS → increase target.activation by E.strength * source.confidence
   b. If E.kind == CONTRADICTS → increase target.tension
   c. If E.kind == REQUIRES → propagate only if all dependencies satisfied
   d. If E.kind == IMPLIES → cascade with decay factor 0.7
4. COMPUTE tension per dimension:
   tension(d) = Σ(supporting.weight) - Σ(contradicting.weight * contradictionMultiplier)
5. RESOLVE confidence:
   confidence(d) = sigmoid(activation(d) - tension(d) * tensionSensitivity)
6. APPLY decay since last evaluation
7. EMIT ConfidenceReceipt for each delta > epsilon
8. UPDATE RiskGraph rollup
```

### 8.5 Contradiction Multipliers (configurable)

| Contradiction Source | Default Multiplier | Rationale |
|---------------------|-------------------|-----------|
| Server lag spike (> 200ms) | 0.85 | High ping explains movement anomalies |
| Recent teleport | 0.90 | Position discontinuity expected |
| Creative mode | 0.95 | Flight allowed |
| Staff freeze active | 1.00 | No reduction — inspection mode |
| Recent damage knockback | 0.75 | Velocity spike explained |

---

## 9. Event Flow

### 9.1 Primary Evidence Pipeline

```text
[Paper Event]
     │
     ▼
[Capture Adapter] ── creates Sample (pooled object)
     │
     ▼
[EventBus.publish(PlayerSampleEvent)]
     │
     ├──► [ReplayEngine] (optional capture)
     │
     ▼
[CheckEngine.schedule(sample)]
     │
     ▼ (async worker)
[Check.evaluate(context)] ── returns List<Evidence>
     │
     ▼
[EventBus.publish(EvidenceProducedEvent)]
     │
     ▼
[EvidenceEngine.ingest(evidence)]
     │
     ▼
[GraphEngine.propagate(evidence)]
     │
     ▼
[StateEngine.applyMutations(playerState)]
     │
     ├──► [StorageEngine.enqueue(receipts, evidence)]
     │
     ▼
[NotificationEngine.evaluate(thresholds)]
     │
     ▼
[Alert if confidence > threshold AND trend rising]
```

### 9.2 Explainability Query Flow

```text
Staff: /ts inspect <player>
     │
     ▼
[CommandEngine] → [TrustEvaluationService.explain(playerId)]
     │
     ▼
[ExplainabilityService]
     ├── load PlayerState
     ├── for each ConfidenceDimension:
     │     ├── top 5 supporting evidence (by weight * recency)
     │     ├── top 5 conflicting evidence
     │     └── compute net confidence with decay
     └── build ExplanationReport
     │
     ▼
[GuiEngine.openInspectGui(report)]
```

---

## 10. Plugin Lifecycle

### 10.1 Bootstrap Sequence

```text
onLoad()
  ├── load config (YAML + validation)
  ├── init DI ServiceRegistry
  ├── register storage adapter (SQLite default)
  ├── run Flyway migrations
  └── init metrics

onEnable()
  ├── start EventBus
  ├── start engines (order matters):
  │     StorageEngine → StateEngine → GraphEngine → EvidenceEngine
  │     → CheckEngine → ReplayEngine → NotificationEngine
  │     → GuiEngine → CommandEngine
  ├── register Paper listeners
  ├── register packet listeners (if ProtocolLib present)
  ├── register commands
  ├── register check modules from config
  ├── start schedulers (decay, batch flush)
  └── publish PluginReadyEvent

onDisable()
  ├── stop accepting new samples
  ├── flush storage queues (timeout 5s)
  ├── stop schedulers
  ├── stop worker pools
  ├── persist in-memory player states
  └── shutdown metrics
```

### 10.2 Player Lifecycle

```text
PlayerJoin
  ├── StateEngine.createOrLoad(uuid)
  ├── seed EnvironmentState (world, gamemode)
  ├── seed LatencyState
  └── HistoryGraph.addNode(FIRST_SESSION or RETURNING)

PlayerQuit
  ├── flush pending receipts
  ├── StateEngine.snapshot(uuid)
  ├── StorageEngine.persist(state)
  └── pool.return(PlayerState)

StaffFreeze
  ├── StaffState.frozen = true
  ├── ReplayEngine.startContinuousCapture(uuid)
  └── NotificationEngine.notifyStaff
```

---

## 11. Engine Specifications

### 11.1 Check Engine

- **Registry:** `Map<CheckId, Check>` loaded from config
- **Scheduling:** Per-player ring buffer of pending samples; worker claims batches
- **Context:** Immutable `CheckContext` with PlayerState snapshot, sample, config slice
- **Output:** `CheckResult { List<Evidence> evidence; Duration elapsed; }` — never punishment
- **Isolation:** Check exceptions caught, logged, emit `CHECK_FAILURE` evidence with severity 0

### 11.2 Evidence Engine

- Deduplication window: 500ms per (player, kind, sourceCheck)
- Normalization: severity clamped, credibility weighted
- Linking: auto-link related evidence within temporal window
- Validation: reject evidence with empty `reason`

### 11.3 Graph Engine

- In-memory per-player graphs (not shared across players)
- Max nodes per graph: 10,000 (configurable); LRU prune of expired nodes
- Propagation budget: 2ms per player per batch

### 11.4 State Engine

- Copy-on-write mutations
- Version counter for optimistic concurrency with storage
- Publishes `ConfidenceChangedEvent` on main thread via scheduled sync

### 11.5 Replay Engine

- Ring buffer: 30 seconds default at 20 TPS
- Continuous capture on suspicion threshold breach
- Formats: binary frames (primary), JSON export (secondary)

### 11.6 Storage Engine

- Write-behind cache
- Batch size: 100 receipts or 50ms, whichever first
- Backpressure: drop oldest non-critical writes under queue pressure

### 11.7 Notification Engine

- Rich alert template (see §14)
- Rate limit: 1 alert per player per dimension per 30s
- Trend-aware: suppress if confidence falling

### 11.8 GUI Engine

- Inventory-based GUIs (Paper Adventure API)
- Async data fetch; sync open on main thread
- Pagination for evidence lists

### 11.9 Command Engine

- Brigadier-based `/ts` tree
- Tab completion from online players and check IDs
- Permission gates per subcommand

---

## 12. Check Framework

### 12.1 Check Interface

```java
public interface Check {
    CheckId id();
    CheckCategory category();
    ConfidenceDimension dimension();
    boolean isEnabled(CheckConfig config);
    List<Evidence> evaluate(CheckContext context);
}
```

### 12.2 Example: ImpossibleMovementCheck (pseudocode)

```java
public final class ImpossibleMovementCheck extends AbstractMovementCheck {
    @Override
    public List<Evidence> evaluate(CheckContext ctx) {
        MovementSample current = ctx.sample();
        MovementSample previous = ctx.state().movement().lastSample();
        double maxAccel = ctx.config().getDouble("max-acceleration");
        double observed = acceleration(previous, current);
        if (observed <= maxAccel) return List.of();

        return List.of(Evidence.builder()
            .kind(EvidenceKind.IMPOSSIBLE_ACCELERATION)
            .dimension(ConfidenceDimension.MOVEMENT)
            .severity(normalize(observed, maxAccel))
            .reason("Acceleration %.2f exceeds limit %.2f m/tick²".formatted(observed, maxAccel))
            .payload(Map.of("observed", observed, "limit", maxAccel, "tick", current.tick()))
            .build());
    }
}
```

### 12.3 Check Categories

| Category | Checks |
|----------|--------|
| **Movement** | ImpossibleMovement, Fly, Speed, Jesus, Scaffold, NoSlow, Timer |
| **Combat** | Reach, KillAura, Aim, Velocity, AutoClicker |
| **Inventory** | FastBreak, FastPlace, Xray heuristic |
| **Networking** | BadPackets, Blink |
| **Automation** | Macro detection, pattern repetition |
| **Interaction** | Invalid block interact, reach place |

---

## 13. Machine Learning Interfaces

All ML integration is **optional** and behind ports. Default implementation is no-op.

```java
public interface InferenceProvider {
    String providerId();
    InferenceResult infer(InferenceRequest request);
}

public interface PredictionProvider {
    PredictionResult predict(PredictionRequest request);
}

public interface EvidenceExporter {
    void export(EvidenceExportBatch batch, ExportTarget target);
}

public interface FeatureExtractor {
    float[] extract(PlayerState state, TimeWindow window);
}

public interface SequenceBuilder {
    float[][] buildMovementSequence(List<MovementSample> samples);
}
```

### ML-Ready Export Schema

Exports include: `playerId`, `timeWindow`, `confidenceVector`, `evidenceGraph` (serialized), `movementSequence`, `combatSequence`, `label` (optional staff annotation). Versioned JSON-Lines format.

---

## 14. Staff Tools

### 14.1 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/ts inspect <player>` | `tsspear.inspect` | Live confidence breakdown + top evidence |
| `/ts graph <player> [type]` | `tsspear.graph` | Open graph visualization GUI |
| `/ts evidence <player> [page]` | `tsspear.evidence` | Paginated evidence viewer |
| `/ts timeline <player>` | `tsspear.timeline` | Chronological event timeline |
| `/ts trust <player>` | `tsspear.trust` | Trust state with trend |
| `/ts suspicion <player>` | `tsspear.suspicion` | Suspicion rollup |
| `/ts replay <player> [time]` | `tsspear.replay` | Replay suspicious sequence |
| `/ts freeze <player>` | `tsspear.freeze` | Freeze + continuous replay |
| `/ts alerts [on\|off]` | `tsspear.alerts` | Toggle personal alerts |
| `/ts checks [list\|toggle]` | `tsspear.checks` | Check management |
| `/ts debug <player>` | `tsspear.debug` | Raw state dump (staff only) |
| `/ts export <player>` | `tsspear.export` | JSON export for ML/analysis |

### 14.2 Rich Notification Format

```text
┌─────────────────────────────────────────────┐
│ ⚠ TS-Spear Alert                            │
├─────────────────────────────────────────────┤
│ Player: Steve                               │
│ Dimension: Movement                         │
│ Confidence: 0.82 (↑ 0.14 from 0.68)         │
│ Trend: RISING                               │
│ Evidence Count: 7 (5 support, 2 conflict)   │
├─────────────────────────────────────────────┤
│ Supporting:                                 │
│  • Impossible acceleration (0.91)           │
│  • Repeated pattern (0.74)                  │
│  • Air time anomaly (0.68)                  │
├─────────────────────────────────────────────┤
│ Conflicting:                                │
│  • Server lag spike (0.45)                  │
│  • Recent teleport (0.32)                   │
├─────────────────────────────────────────────┤
│ Net Confidence: 0.71                        │
│ [Inspect] [Replay] [Freeze]                 │
└─────────────────────────────────────────────┘
```

---

## 15. Storage & Persistence

### 15.1 Supported Backends

| Backend | Use Case | Default |
|---------|----------|---------|
| SQLite | Single server, dev | ✓ |
| MySQL / MariaDB | Multi-server | |
| PostgreSQL | Analytics-heavy | |
| JSON export | ML pipelines, manual audit | |

### 15.2 What Gets Persisted

- `ConfidenceReceipt` — always
- `Evidence` — if severity > threshold or staff-flagged
- `PlayerState` snapshot — on quit and periodic (5 min)
- `ReplaySequence` — on alert or staff request
- `StaffAction` — freeze, export, annotation

### 15.3 Migration Strategy

Flyway versioned migrations. Schema version in `tsspear_meta` table. Evidence/replay formats carry `schemaVersion` field for forward compatibility.

---

## 16. Configuration

```text
plugins/TSSpear/
├── config.yml              # Main config
├── checks/
│   ├── movement.yml
│   ├── combat.yml
│   ├── inventory.yml
│   ├── networking.yml
│   └── automation.yml
├── decay.yml               # Per-dimension decay curves
├── thresholds.yml          # Alert levels
├── storage.yml             # Backend config
├── gui.yml                 # GUI theming
├── replay.yml              # Capture settings
├── messages/
│   ├── en_US.yml
│   └── ...
└── permissions.yml         # Permission defaults
```

Key config sections: `engine`, `checks`, `decay`, `thresholds`, `storage`, `replay`, `notifications`, `performance`, `ml`.

---

## 17. Performance Architecture

### 17.1 Allocation Strategy

- **Object pools:** `MovementSample`, `Evidence`, `GraphNode` via Apache Commons Pool or custom ring-buffer pools
- **Primitive arrays:** `FeatureExtractor` uses `float[]` not `List<Double>`
- **Immutable records:** post-mutation, share freely across threads
- **Batch updates:** graph propagation processes evidence in batches of 16

### 17.2 Scalability Targets

| Metric | Target (500 players) |
|--------|---------------------|
| Main thread overhead | < 5ms/tick total |
| Memory per player | < 2MB active state |
| Evidence throughput | 50,000 events/sec cluster |
| Storage write latency | p99 < 200ms async |
| Alert latency | < 500ms from evidence |

### 17.3 Profiling

- Built-in `/ts debug --profile` for hot-path timing
- Optional Spark/Paper timings integration
- Metrics: `tsspear_check_duration`, `tsspear_graph_propagation`, `tsspear_storage_queue_depth`

---

## 18. Developer Experience

### 18.1 Documentation Deliverables

| Document | Location |
|----------|----------|
| Architecture (this doc) | `docs/refs/ts-spear/` |
| Lifecycle guide | `docs/lifecycle.md` |
| Contribution guide | `CONTRIBUTING.md` |
| Testing guide | `TESTING.md` |
| Check authoring guide | `docs/check-authoring.md` |
| API JavaDoc | generated to `docs/javadoc/` |

### 18.2 Diagrams

PlantUML sources ship in [`reference-implementations/ts-spear/docs/architecture/diagrams/`](../../reference-implementations/ts-spear/docs/architecture/diagrams/).

### 18.3 Extension Points

Third-party checks via `api/check/Check` SPI. Register in `checks/custom.yml`.

---

## 19. Testing Strategy

| Layer | Framework | Examples |
|-------|-----------|----------|
| Unit | JUnit 5 | `TensionCalculatorTest`, `DecayApplicatorTest` |
| Integration | JUnit 5 + Testcontainers | `SqliteReceiptStoreTest` |
| Mock Paper | MockBukkit / Paper-test | `MovementListenerTest` |
| Replay | Custom harness | `ReplayRoundTripTest` |
| Stress | JMH | `GraphPropagationBenchmark` |
| Architecture | Konsist | `LayerRulesTest` |

---

## 20. Gradle Project Layout

```text
ts-spear/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
├── buildSrc/
│   └── src/main/kotlin/
│       └── tsspear.conventions.gradle.kts
├── api/                              # Published API module
│   └── build.gradle.kts
├── core/                             # domain + application
│   └── build.gradle.kts
├── checks/                           # check modules (multi-project)
│   ├── movement/
│   ├── combat/
│   └── ...
├── plugin/                           # Paper plugin entry, infrastructure
│   └── build.gradle.kts
├── benchmarks/
│   └── build.gradle.kts
└── tests/
    ├── unit/
    ├── integration/
    └── mock-paper/
```

### Key Dependencies

- Paper API 1.21.x (compileOnly)
- Adventure API (bundled)
- HikariCP + Flyway
- Caffeine (caching)
- Jackson (serialization)
- JUnit 5 + Testcontainers + MockBukkit
- Konsist (architecture tests)
- JMH (benchmarks)

---

## 21. GUI Wireframes

See [gui-wireframes.md](./gui-wireframes.md) for ASCII wireframes of:

- Inspect GUI
- Graph visualization
- Evidence viewer
- Timeline
- Replay controls
- Trust/suspicion dashboard

---

## 22. Format Specifications

Detailed schemas in `./schemas/`:

| File | Description |
|------|-------------|
| `evidence.schema.json` | Evidence serialization |
| `receipt.schema.json` | ConfidenceReceipt |
| `replay.schema.json` | Replay frame format |
| `export.schema.json` | ML export batch |
| `database.sql` | DDL for all backends |

---

## 23. Roadmap — MVP to Enterprise

### Phase 0 — Foundation (Weeks 1-4)
- Gradle multi-module scaffold
- DI, EventBus, domain models
- StateEngine + in-memory storage
- Single movement check (ImpossibleMovement)
- `/ts inspect` command (text only)
- Konsist layer tests

### Phase 1 — MVP (Weeks 5-10)
- Evidence + Graph engines
- 5 movement checks, 3 combat checks
- SQLite persistence + receipts
- Basic alerts
- Decay (linear only)
- Replay ring buffer (30s)

### Phase 2 — Intelligence (Weeks 11-18)
- Full graph suite (5 graph types)
- Contradiction resolution
- Rich notifications
- GUI suite (inspect, evidence, timeline)
- Exponential/logarithmic decay
- JSON export

### Phase 3 — Staff Platform (Weeks 19-26)
- Replay viewer GUI
- Freeze + continuous capture
- Graph visualization
- MySQL/Postgres backends
- Permission + localization
- Check management GUI

### Phase 4 — Scale (Weeks 27-34)
- 500+ player optimization
- Object pooling
- JMH benchmarks + profiling
- Packet-level checks (ProtocolLib)
- Stress test suite

### Phase 5 — ML-Ready (Weeks 35-42)
- FeatureExtractor + SequenceBuilder
- EvidenceExporter pipeline
- InferenceProvider SPI
- Staff annotation → training labels
- External model hook documentation

### Phase 6 — Enterprise (Weeks 43+)
- Multi-server state federation
- Central dashboard (web, out of plugin scope)
- Custom check marketplace
- Advanced ML model integration
- Compliance audit exports

---

## 24. Key Decisions

| # | Decision | Rationale |
|---|----------|-----------|
| KD-1 | **Checks emit Evidence, never punish** | Separates detection from enforcement; staff retain authority; aligns with SPEAR "prove before act" |
| KD-2 | **Hexagonal architecture with Konsist enforcement** | Borrowed from spear-plugin; keeps domain testable without Paper |
| KD-3 | **Five graph types, not one mega-graph** | Separation of concerns: evidence linking ≠ constraint satisfaction ≠ risk rollup |
| KD-4 | **Confidence is per-dimension, not global boolean** | Explainability requires decomposed belief states |
| KD-5 | **Contradiction edges reduce tension, not delete evidence** | Lag/teleport explain without erasing suspicious signals |
| KD-6 | **Copy-on-write PlayerState with version counter** | Safe concurrent access between main and worker threads |
| KD-7 | **Write-behind async storage** | 500+ player scale requires zero main-thread I/O |
| KD-8 | **Binary replay format primary, JSON secondary** | Size and seek performance for 20 TPS capture |
| KD-9 | **ML behind ports with no-op defaults** | AI-ready without requiring ML at MVP |
| KD-10 | **Flyway migrations from day one** | Schema evolution is inevitable for evidence/replay formats |
| KD-11 | **ProtocolLib optional, not required** | Reduces deployment friction; packet checks activate when present |
| KD-12 | **Gradle Kotlin multi-module** | Clean separation of api/core/checks/plugin; matches modern Java ecosystem |

---

## 25. PR Plan

### PR-1: Project Scaffold
- **Files:** `settings.gradle.kts`, `build.gradle.kts`, `buildSrc/`, empty modules
- **Dependencies:** none
- **Description:** Multi-module Gradle Kotlin project with Paper API, JUnit, Konsist

### PR-2: Domain Models
- **Files:** `core/src/.../domain/model/**`
- **Dependencies:** PR-1
- **Description:** Evidence, PlayerState, graphs, receipts, confidence types

### PR-3: Ports & Domain Services
- **Files:** `core/src/.../domain/port/**`, `domain/service/**`
- **Dependencies:** PR-2
- **Description:** Repository ports, ConfidenceResolver, TensionCalculator, DecayApplicator

### PR-4: Event Bus & DI
- **Files:** `core/src/.../application/bus/**`, `application/di/**`
- **Dependencies:** PR-2
- **Description:** EventBus, ServiceRegistry, Module interface

### PR-5: State Engine
- **Files:** `core/src/.../application/engine/StateEngine.java`
- **Dependencies:** PR-3, PR-4
- **Description:** PlayerState lifecycle, copy-on-write mutations

### PR-6: Evidence & Graph Engines
- **Files:** `EvidenceEngine.java`, `GraphEngine.java`
- **Dependencies:** PR-5
- **Description:** Evidence ingestion, graph propagation, tension resolution

### PR-7: Check Framework
- **Files:** `api/check/**`, `checks/movement/**`, `CheckEngine.java`
- **Dependencies:** PR-6
- **Description:** Check SPI, registry, ImpossibleMovementCheck

### PR-8: Paper Plugin Bootstrap
- **Files:** `plugin/**`, `TSSpearPlugin.java`, listeners
- **Dependencies:** PR-7
- **Description:** onEnable/onDisable, movement capture, sample publishing

### PR-9: Storage Engine (SQLite)
- **Files:** `infrastructure/storage/sqlite/**`, `database.sql`, Flyway migrations
- **Dependencies:** PR-5
- **Description:** Receipt persistence, player state snapshots

### PR-10: Commands & Text Inspect
- **Files:** `infrastructure/command/**`
- **Dependencies:** PR-8
- **Description:** `/ts inspect`, `/ts trust`, `/ts suspicion`

### PR-11: Notification Engine
- **Files:** `NotificationEngine.java`, `messages/en_US.yml`
- **Dependencies:** PR-6
- **Description:** Rich alerts with confidence breakdown

### PR-12: Replay Engine
- **Files:** `ReplayEngine.java`, replay schema
- **Dependencies:** PR-8
- **Description:** Ring buffer capture, binary format

### PR-13: Decay Scheduler
- **Files:** `DecayScheduler.java`, `decay.yml`
- **Dependencies:** PR-6
- **Description:** Linear, exponential, logarithmic decay curves

### PR-14: GUI Suite
- **Files:** `infrastructure/gui/**`, `gui.yml`
- **Dependencies:** PR-10
- **Description:** Inspect, evidence, timeline, graph GUIs

### PR-15: Additional Checks
- **Files:** `checks/combat/**`, `checks/inventory/**`, etc.
- **Dependencies:** PR-7
- **Description:** Reach, Fly, Speed, KillAura, BadPackets, etc.

### PR-16: ML Export Interfaces
- **Files:** `application/ml/**`, `export.schema.json`
- **Dependencies:** PR-6
- **Description:** FeatureExtractor, EvidenceExporter, InferenceProvider SPI

### PR-17: Performance & Benchmarks
- **Files:** `benchmarks/**`, object pools
- **Dependencies:** PR-6
- **Description:** JMH benchmarks, pooling, profiling hooks

### PR-18: Multi-DB Support
- **Files:** `infrastructure/storage/mysql/**`, `postgres/**`
- **Dependencies:** PR-9
- **Description:** MySQL, MariaDB, PostgreSQL adapters

### PR-19: Documentation & Diagrams
- **Files:** `docs/**`, PlantUML, JavaDoc config
- **Dependencies:** all prior
- **Description:** Lifecycle docs, contribution guide, rendered diagrams

### PR-20: Integration & Stress Tests
- **Files:** `tests/**`
- **Dependencies:** PR-8
- **Description:** MockBukkit tests, replay round-trip, stress harness

---

*End of architecture document. No implementation code has been written.*

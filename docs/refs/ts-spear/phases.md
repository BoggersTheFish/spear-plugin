# Implementation Phases

Delivery status for TS-Spear from MVP through ML-ready.

## Phase 0 — Foundation ✅

- Gradle Kotlin multi-module scaffold
- Hexagonal layering (`api`, `core`, `plugin`)
- Domain models: `PlayerState`, `Evidence`, `ConfidenceVector`
- `StateEngine`, `EventBus`, in-memory storage
- `ImpossibleMovementCheck`
- `/ts inspect` (text)
- Konsist-ready module boundaries

## Phase 1 — MVP ✅

- `EvidenceEngine`, `GraphEngine`, `CheckEngine`
- Movement checks: impossible movement, lag spike, fly, speed
- SQLite persistence (Flyway V1)
- `ConfidenceReceipt` audit trail
- Decay scheduler (per-dimension curves)
- Replay ring buffer (30s)
- Text commands: trust, suspicion, evidence, replay

## Phase 2 — Intelligence ✅

- Combat checks: reach, killaura, velocity
- Full graph suite (EVIDENCE, CONSTRAINT, TENSION, RISK, HISTORY)
- Contradiction resolution via `TensionCalculator`
- GUI suite: Inspect, Evidence, Timeline, Graph
- `ExportService` + `JsonEvidenceExporter`
- `FeatureExtractor` (basic), `SequenceBuilder`
- Rich Adventure staff alerts
- Commands: graph, timeline, export

## Phase 3 — Staff Platform ✅

- `FreezeService` + continuous replay capture
- `ReplayGui`, `ChecksGui`, graph type tabs
- `storage-jdbc` (MySQL + PostgreSQL)
- `MessageService` + `messages/en_US.yml`
- Commands: freeze, unfreeze, alerts, checks

## Phase 4 — Scale ✅

- Object pools: `GraphNode`, `Evidence.Builder`, `MovementSampleScratch`
- `PerformanceMetrics` wired into check/graph/storage hot paths
- `/ts debug profile` performance snapshot
- Packet checks: `BadPacketCheck`, `BlinkCheck`
- Optional ProtocolLib bridge
- JMH `GraphPropagationBenchmark`
- `GraphPropagationStressTest` (500 players)
- Config: `performance.*`, `checks.networking.*`

## Phase 5 — ML-Ready ✅

- Enhanced `FeatureExtractor` (17 named features)
- `SequenceBuilder` packet sequences
- Schema v1 `MlExportDocument` (JSON + JSONL)
- `RuleBasedInferenceProvider` + `MlProviderRegistry`
- `AnnotationService` + `ts_staff_annotation` (Flyway V2)
- Commands: `/ts annotate`, `/ts infer`, `/ts export jsonl`
- [ML Hooks](./ml-hooks.md) external integration guide

## Phase 6 — Enterprise (Planned)

- Multi-server state federation
- Central web dashboard (out of plugin scope)
- Custom check marketplace
- Advanced ML model integration (online `InferenceProvider` plugins)
- Compliance audit exports

## Build Verification

```bash
./gradlew build   # All phases 0–5 pass
```

Plugin artifact: `plugin/build/libs/TSSpear-0.1.0-SNAPSHOT.jar`

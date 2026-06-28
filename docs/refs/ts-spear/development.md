# Development Guide

## Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Build | Gradle 8.12 (Kotlin DSL) |
| Server API | Paper 1.21 |
| Persistence | SQLite (default), MySQL, PostgreSQL via Flyway |
| JSON | Jackson 2.18 |
| Testing | JUnit 5 |
| Benchmarks | JMH |

## Module Layout

```
ts-spear/
├── api/                 # Public API types (ConfidenceDimension, GraphKind, etc.)
├── core/                # Domain, application engines, services
├── storage-sqlite/      # SQLite + Flyway migrations
├── storage-jdbc/        # MySQL/PostgreSQL adapters
├── checks/
│   ├── movement/        # Movement checks
│   ├── combat/          # Combat checks
│   └── networking/      # Packet checks
├── benchmarks/          # JMH graph propagation benchmarks
└── plugin/              # Paper plugin (Bukkit edge)
```

### Dependency Rule (Hexagonal)

```
domain → (nothing external)
application → domain
infrastructure/plugin → application + domain
checks → domain + api
```

## Build Commands

```bash
./gradlew build                    # Full build + tests
./gradlew :plugin:shadowJar        # Plugin JAR only
./gradlew :benchmarks:jmh            # Run JMH benchmarks
./gradlew :core:test                 # Core unit tests
./gradlew :storage-sqlite:test       # SQLite integration tests
```

## Testing Strategy

| Layer | Tests |
|-------|-------|
| Domain services | `ConfidenceResolverTest`, `DecayApplicatorTest` |
| Engines | `ReplayEngineTest`, `FreezeServiceTest` |
| Checks | Per-check unit tests in `checks:*` |
| ML | `FeatureExtractorTest`, `RuleBasedInferenceProviderTest`, `ExportServiceTest` |
| Scale | `GraphPropagationStressTest` (500 players) |

## Adding a Check

1. Extend `AbstractMovementCheck`, `AbstractCombatCheck`, or `AbstractNetworkingCheck`.
2. Register in `TSSpearPlugin` check list.
3. Add config keys to `TSSpearSettings` + `PluginConfigLoader` + `config.yml`.
4. Enable in `TSSpearRuntime.buildCheckConfig()`.
5. Write unit test with synthetic samples.

Checks **must return `Evidence`**, never call ban/kick.

## Key Engines

| Engine | Responsibility |
|--------|----------------|
| `StateEngine` | PlayerState lifecycle, copy-on-write mutations |
| `CheckEngine` | Async check dispatch on worker pool |
| `EvidenceEngine` | Ingest, dedupe, weight evidence |
| `GraphEngine` | Propagate evidence through 5 graph types |
| `StorageEngine` | Write-behind receipt/evidence persistence |
| `ReplayEngine` | Ring buffer + continuous capture |
| `NotificationEngine` | Staff alert routing |

## Local Paper Testing

1. Build shadow JAR.
2. Drop into a Paper 1.21 test server's `plugins/`.
3. Join as player, trigger movement/combat.
4. Run `/ts inspect` and `/ts debug profile`.

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md).

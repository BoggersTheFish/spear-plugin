# Configuration Reference

All settings live in `plugins/TSSpear/config.yml`. Defaults are written on first boot.

## Top-Level

| Key | Default | Description |
|-----|---------|-------------|
| `schema-version` | `1` | Config schema version |
| `locale` | `en_US` | Message bundle (`messages/en_US.yml`) |

## Engine

```yaml
engine:
  worker-threads: 2
  evidence-dedupe-window-ms: 500
```

| Key | Description |
|-----|-------------|
| `worker-threads` | Async check worker pool size |
| `evidence-dedupe-window-ms` | Deduplicate identical evidence within window |

## Storage

```yaml
storage:
  backend: SQLITE          # SQLITE | MYSQL | POSTGRESQL | MEMORY
  batch-size: 100
  flush-interval-ms: 50
  evidence-persist-threshold: 0.25
```

See [Storage](./storage.md) for JDBC connection blocks.

## Checks

### Movement

| Key | Default | Check |
|-----|---------|-------|
| `max-acceleration` | `0.52` | ImpossibleMovement |
| `max-speed` | `0.65` | Speed |
| `max-air-ticks` | `40` | Fly |

### Combat

| Key | Default | Check |
|-----|---------|-------|
| `max-reach` | `3.2` | Reach |
| `killaura-window-ticks` | `10` | KillAura window |
| `killaura-victim-threshold` | `3` | KillAura victims/tick |
| `max-knockback-damage-ratio` | `0.5` | Velocity |

### Networking

| Key | Default | Check |
|-----|---------|-------|
| `blink-min-interval-ms` | `40` | Blink |
| `bad-packet-position-threshold` | `0.5` | BadPacket |

## Performance (Phase 4)

```yaml
performance:
  object-pooling-enabled: true
  graph-node-pool-size: 512
  evidence-builder-pool-size: 256
  movement-scratch-pool-size: 128
  graph-propagation-batch-size: 16
```

## ML (Phase 5)

```yaml
ml:
  export-window-seconds: 300
  inference-provider: rule-based    # rule-based | noop
  rule-based:
    cheating-threshold: 0.65
    suspicious-threshold: 0.40
```

See [ML Hooks](./ml-hooks.md) for export and inference details.

## Graph & Thresholds

```yaml
graph:
  contradiction-multiplier: 0.85
  tension-sensitivity: 1.2
  confidence-epsilon: 0.005

thresholds:
  watch: 0.35
  alert: 0.65
  elevate: 0.80
```

| Threshold | Effect |
|-----------|--------|
| `watch` | Elevated monitoring (future hooks) |
| `alert` | Staff notification + replay capture trigger |
| `elevate` | High-priority staff routing (future) |

## Replay

```yaml
replay:
  ring-buffer-seconds: 30
  ticks-per-second: 20
  continuous-capture-seconds: 300
```

## Decay

Per-dimension decay curves (`EXPONENTIAL`, `LINEAR`, `LOGARITHMIC`):

```yaml
decay:
  movement:
    curve: EXPONENTIAL
    rate: 0.02
  combat:
    curve: LINEAR
    rate: 0.01
  # ... inventory, automation, networking, interaction
```

## Networking (Latency)

```yaml
networking:
  lag-threshold-ms: 200
```

Used by `LagSpikeCheck` and latency-aware evidence weighting.

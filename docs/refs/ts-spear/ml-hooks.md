# TS-Spear ML Hooks

Phase 5 exposes stable ports and export formats for external machine-learning pipelines without coupling models to the plugin runtime.

## Export Pipeline

Staff export via `/ts export [jsonl] [player]` writes schema v1 documents to `plugins/TSSpear/exports/`.

| Field | Description |
|-------|-------------|
| `schemaVersion` | Always `1` for current format |
| `timeWindow` | Rolling window (`ml.export-window-seconds`, default 300s) |
| `confidenceVector` | Per-dimension confidence + aggregate |
| `evidenceGraph` | Serialized EVIDENCE, TENSION, RISK, CONSTRAINT, HISTORY graphs |
| `movementSequence` | `[x, y, z, velocity, onGround, ping]` per sample |
| `combatSequence` | `[distance, damage, critical, ping]` per hit |
| `packetSequence` | `[positionDelta, ping, intervalMs, reportedX, serverX]` per packet |
| `features` | Flat float vector (17 named features) |
| `featureNames` | Parallel name list for `features` |
| `label` | Latest staff annotation when present |

JSONL mode (`/ts export jsonl <player>`) emits one document per line for batch ingestion.

Schema: [`docs/schemas/export.schema.json`](./schemas/export.schema.json)

## Feature Vector

`FeatureExtractor` produces 17 features:

- Six dimension confidences (movement → interaction)
- `suspicion_risk`, `suspicion_delta`, `aggregate_confidence`
- `tension_aggregate`, `tension_graph_nodes`, `tension_graph_avg_activation`
- `packet_count`, `packet_avg_delta`, `packet_max_delta`, `packet_avg_interval_ms`
- `evidence_recent_count`

## Staff Labels

`/ts annotate <player> <verdict> [notes]` persists training labels:

| Verdict | Use |
|---------|-----|
| `LEGIT` | Confirmed false positive |
| `SUSPICIOUS` | Needs review; weak positive |
| `CHEATING` | Confirmed violation |
| `INCONCLUSIVE` | Insufficient evidence |

Labels attach automatically to the next export for that player via the `label` field.

Storage: `ts_staff_annotation` table (Flyway V2).

## Inference SPI

```java
public interface InferenceProvider {
    String providerId();
    InferenceResult infer(InferenceRequest request);
}
```

Built-in providers:

| ID | Behavior |
|----|----------|
| `noop` | Always abstains |
| `rule-based` | Threshold classifier over feature vector (default) |

Configure active provider in `config.yml`:

```yaml
ml:
  inference-provider: rule-based
  rule-based:
    cheating-threshold: 0.65
    suspicious-threshold: 0.40
```

Staff command: `/ts infer [player]` runs the active provider and prints label + confidence.

## External Model Integration

1. Implement `InferenceProvider` in a separate JAR or module.
2. Register via `MlProviderRegistry` at bootstrap (future plugin hook) or replace the active provider ID in config once wired.
3. Consume exports offline: read JSON/JSONL from `exports/`, train on `features` + `label.verdict`.
4. For online scoring, call `infer(InferenceRequest)` with the same feature order as `FeatureExtractor.FEATURE_NAMES`.

`InferenceRequest` fields:

- `playerId` — UUID
- `features` — `float[]` aligned to `featureNames`
- `modelHint` — optional string for multi-model routing

`InferenceResult` fields:

- `abstain` — provider declined to score
- `confidence` — 0.0–1.0
- `label` — `LEGIT`, `SUSPICIOUS`, `CHEATING`, or `ABSTAIN`
- `reason` — human-readable explanation
- `metadata` — optional structured extras

## Prediction Provider

`PredictionProvider` remains a no-op stub for forward-compatible sequence forecasting. Wire in Phase 6+ when temporal models are added.

## Recommended Offline Workflow

1. Staff review suspicious players; annotate verdicts.
2. Export labeled batches (`/ts export jsonl <player>`).
3. Concatenate JSONL files into a training corpus.
4. Train classifier on `features` → `label.verdict`.
5. Deploy model behind a custom `InferenceProvider` implementation.
6. Set `ml.inference-provider` to your provider ID.

Nothing in this pipeline triggers automatic punishment — inference informs staff recommendations only.
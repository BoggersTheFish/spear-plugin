# TS-Spear Core Interfaces & Example Implementations

Design-time interface contracts. These are **specifications**, not compiled code. Implementation follows in PR-2 through PR-7.

---

## 1. Domain Ports (Hexagonal)

### Check

```java
package com.badgersmc.tsspear.domain.check;

public interface Check {
    CheckId id();
    CheckCategory category();
    ConfidenceDimension dimension();
    boolean isEnabled(CheckConfig config);
    List<Evidence> evaluate(CheckContext context);
}
```

### Repository Ports

```java
package com.badgersmc.tsspear.domain.port;

public interface PlayerStateRepository {
    Optional<PlayerState> findById(UUID playerId);
    void save(PlayerState state);
    void delete(UUID playerId);
}

public interface EvidenceStore {
    void store(Evidence evidence);
    List<Evidence> findByPlayer(UUID playerId, QueryOptions options);
    Optional<Evidence> findById(EvidenceId id);
}

public interface ReceiptStore {
    void store(ConfidenceReceipt receipt);
    List<ConfidenceReceipt> findByPlayer(UUID playerId, TimeRange range);
}

public interface ReplayStore {
    void store(ReplaySequence sequence);
    Optional<ReplaySequence> findById(UUID sequenceId);
}
```

### ML Ports

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

---

## 2. Abstract Base Classes

### AbstractCheck

```java
package com.badgersmc.tsspear.domain.check;

public abstract class AbstractCheck implements Check {
    private final CheckId id;
    private final CheckCategory category;
    private final ConfidenceDimension dimension;

    protected AbstractCheck(CheckId id, CheckCategory category, ConfidenceDimension dimension) {
        this.id = id;
        this.category = category;
        this.dimension = dimension;
    }

    @Override public final CheckId id() { return id; }
    @Override public final CheckCategory category() { return category; }
    @Override public final ConfidenceDimension dimension() { return dimension; }

    @Override
    public boolean isEnabled(CheckConfig config) {
        return config.isEnabled(id.value());
    }

    protected Evidence buildEvidence(
            EvidenceKind kind,
            double severity,
            double credibility,
            CheckContext ctx,
            String reason,
            Map<String, Object> payload) {
        return Evidence.builder()
            .id(EvidenceId.generate())
            .kind(kind)
            .dimension(dimension)
            .severity(clamp(severity))
            .credibility(clamp(credibility))
            .timestamp(ctx.now())
            .location(ctx.playerLocation())
            .reason(reason)
            .sourceCheckId(id.value())
            .payload(payload)
            .build();
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
```

### AbstractEngine

```java
package com.badgersmc.tsspear.application.engine;

public abstract class AbstractEngine {
    private volatile EngineHealth health = EngineHealth.STOPPED;

    public final void start() {
        if (health == EngineHealth.RUNNING) return;
        onStart();
        health = EngineHealth.RUNNING;
    }

    public final void stop() {
        if (health == EngineHealth.STOPPED) return;
        onStop();
        health = EngineHealth.STOPPED;
    }

    public EngineHealth health() { return health; }

    protected abstract void onStart();
    protected abstract void onStop();
}
```

### AbstractMovementCheck

```java
public abstract class AbstractMovementCheck extends AbstractCheck {
    protected AbstractMovementCheck(CheckId id) {
        super(id, CheckCategory.MOVEMENT, ConfidenceDimension.MOVEMENT);
    }

    protected MovementSample previousSample(CheckContext ctx) {
        return ctx.state().movement().lastSample();
    }

    protected double acceleration(MovementSample prev, MovementSample curr) {
        if (prev == null) return 0.0;
        double dt = Math.max(1, curr.tick() - prev.tick());
        double dv = curr.velocity().length() - prev.velocity().length();
        return Math.abs(dv / dt);
    }
}
```

---

## 3. Engine Interfaces

### EventBus

```java
package com.badgersmc.tsspear.application.bus;

public interface EventBus {
    <T extends TSEvent> void publish(T event);
    <T extends TSEvent> void subscribe(Class<T> type, EventHandler<T> handler);
    void unsubscribe(Class<? extends TSEvent> type, EventHandler<?> handler);
}

@FunctionalInterface
public interface EventHandler<T extends TSEvent> {
    void handle(T event);
}
```

### CheckEngine

```java
public interface CheckEngine {
    void register(Check check);
    void unregister(CheckId id);
    void schedule(PlayerSampleEvent event);
    Collection<Check> registeredChecks();
}
```

### EvidenceEngine

```java
public interface EvidenceEngine {
    List<Evidence> ingest(List<Evidence> raw);
}
```

### GraphEngine

```java
public interface GraphEngine {
    PropagationResult propagate(UUID playerId, List<Evidence> evidence);
}
```

### StateEngine

```java
public interface StateEngine {
    PlayerState getOrCreate(UUID playerId);
    void applyMutations(UUID playerId, StateMutation mutation);
    Optional<PlayerState> remove(UUID playerId);
}
```

---

## 4. Domain Services

### TensionCalculator

```java
package com.badgersmc.tsspear.domain.service;

public final class TensionCalculator {
    public double compute(DimensionConfidence dim, TensionGraph graph, TensionConfig config) {
        double supporting = sumEdgeWeight(graph, dim.dimension(), EdgeKind.SUPPORTS);
        double contradicting = sumEdgeWeight(graph, dim.dimension(), EdgeKind.CONTRADICTS);
        return supporting - (contradicting * config.contradictionMultiplier());
    }
}
```

### DecayApplicator

```java
public final class DecayApplicator {
    public double apply(double confidence, DecayCurve curve, Duration elapsed, double decayRate) {
        return switch (curve) {
            case LINEAR -> Math.max(0, confidence - decayRate * elapsed.toSeconds());
            case EXPONENTIAL -> confidence * Math.exp(-decayRate * elapsed.toSeconds());
            case LOGARITHMIC -> confidence - decayRate * Math.log1p(elapsed.toSeconds());
        };
    }
}
```

### ExplainabilityService

```java
public final class ExplainabilityService {
    public ExplanationReport explain(PlayerState state, ExplainabilityConfig config) {
        List<DimensionExplanation> dimensions = new ArrayList<>();
        for (ConfidenceDimension dim : ConfidenceDimension.values()) {
            DimensionConfidence dc = state.trust().vector().get(dim);
            List<EvidenceSummary> supporting = rankEvidence(
                state, dc.supportingEvidence(), config.maxEvidencePerSide(), true);
            List<EvidenceSummary> conflicting = rankEvidence(
                state, dc.conflictingEvidence(), config.maxEvidencePerSide(), false);
            double net = resolveNetConfidence(dc, supporting, conflicting);
            dimensions.add(new DimensionExplanation(dim, dc.confidence(), net,
                supporting, conflicting, dc.explanation()));
        }
        return new ExplanationReport(state.uuid(), dimensions, state.trust());
    }

    private double resolveNetConfidence(
            DimensionConfidence dc,
            List<EvidenceSummary> supporting,
            List<EvidenceSummary> conflicting) {
        double support = supporting.stream().mapToDouble(EvidenceSummary::weight).sum();
        double conflict = conflicting.stream().mapToDouble(EvidenceSummary::weight).sum();
        return sigmoid(dc.confidence() - conflict * 0.5 + support * 0.1);
    }
}
```

---

## 5. Example Check Implementation

### ImpossibleMovementCheck

```java
package com.badgersmc.tsspear.check.movement;

public final class ImpossibleMovementCheck extends AbstractMovementCheck {
    public static final CheckId ID = CheckId.of("impossible-movement");

    public ImpossibleMovementCheck() {
        super(ID);
    }

    @Override
    public List<Evidence> evaluate(CheckContext ctx) {
        MovementSample current = ctx.sample();
        MovementSample previous = previousSample(ctx);
        if (previous == null) return List.of();

        double maxAccel = ctx.config().getDouble("max-acceleration", 0.52);
        double observed = acceleration(previous, current);

        if (observed <= maxAccel) return List.of();

        double severity = normalize(observed, maxAccel, 2.0);
        return List.of(buildEvidence(
            EvidenceKind.IMPOSSIBLE_ACCELERATION,
            severity,
            0.95,
            ctx,
            "Acceleration %.3f exceeds limit %.3f blocks/tick²".formatted(observed, maxAccel),
            Map.of(
                "observed", observed,
                "limit", maxAccel,
                "tick", current.tick(),
                "from", previous.position(),
                "to", current.position()
            )
        ));
    }

    private double normalize(double observed, double limit, double ceilingFactor) {
        return Math.min(1.0, Math.max(0.0, (observed - limit) / (limit * ceilingFactor)));
    }
}
```

### LagSpikeEvidenceProducer (contradiction source, not a "check")

```java
package com.badgersmc.tsspear.check.networking;

public final class LagSpikeMonitor extends AbstractCheck {
    public LagSpikeMonitor() {
        super(CheckId.of("lag-spike"), CheckCategory.NETWORKING, ConfidenceDimension.MOVEMENT);
    }

    @Override
    public List<Evidence> evaluate(CheckContext ctx) {
        int ping = ctx.state().latency().currentPingMs();
        int threshold = ctx.config().getInt("lag-threshold-ms", 200);
        if (ping < threshold) return List.of();

        return List.of(buildEvidence(
            EvidenceKind.LAG_SPIKE,
            normalize(ping, threshold),
            0.80,
            ctx,
            "Ping %dms exceeds threshold %dms — may explain movement anomalies".formatted(ping, threshold),
            Map.of("ping", ping, "threshold", threshold)
        ));
    }
}
```

Note: `LAG_SPIKE` evidence is linked as **contradicting** movement evidence in the GraphEngine constraint templates.

---

## 6. Configuration Example

### config.yml (excerpt)

```yaml
schema-version: 1

engine:
  worker-threads: 4
  graph-propagation-budget-ms: 2
  evidence-dedupe-window-ms: 500

decay:
  movement:
    curve: EXPONENTIAL
    rate: 0.02
  combat:
    curve: LINEAR
    rate: 0.01

thresholds:
  watch: 0.35
  alert: 0.65
  elevate: 0.80

contradiction-multipliers:
  lag-spike: 0.85
  teleport: 0.90
  creative-mode: 0.95

storage:
  backend: SQLITE
  sqlite:
    file: plugins/TSSpear/data/tsspear.db
  batch-size: 100
  flush-interval-ms: 50

replay:
  ring-buffer-seconds: 30
  continuous-capture-threshold: 0.65

checks:
  movement:
    impossible-movement:
      enabled: true
      max-acceleration: 0.52
    fly-check:
      enabled: true
      max-air-ticks: 40
  combat:
    reach-check:
      enabled: true
      max-reach: 3.2
```

---

## 7. No-Op ML Defaults

```java
public final class NoOpInferenceProvider implements InferenceProvider {
    @Override public String providerId() { return "noop"; }
    @Override public InferenceResult infer(InferenceRequest request) {
        return InferenceResult.abstain("ML provider not configured");
    }
}

public final class NoOpEvidenceExporter implements EvidenceExporter {
    @Override public void export(EvidenceExportBatch batch, ExportTarget target) {
        // JSON file write only when /ts export invoked
    }
}
```

---

*These interfaces define the contracts for implementation. All domain types remain free of Bukkit/Paper imports.*

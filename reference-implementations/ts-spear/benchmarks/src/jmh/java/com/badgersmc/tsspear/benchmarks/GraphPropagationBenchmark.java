package com.badgersmc.tsspear.benchmarks;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.application.config.TSSpearSettings;
import com.badgersmc.tsspear.application.engine.GraphEngine;
import com.badgersmc.tsspear.application.pool.PoolRegistry;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.EvidenceId;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;
import com.badgersmc.tsspear.domain.service.ConfidenceResolver;
import com.badgersmc.tsspear.domain.service.TensionCalculator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class GraphPropagationBenchmark {
    private GraphEngine engine;
    private PlayerState state;
    private List<Evidence> evidenceBatch;

    @Setup
    public void setup() {
        TSSpearSettings settings = TSSpearSettings.defaults();
        PoolRegistry.install(settings);
        engine = new GraphEngine(
            new ConfidenceResolver(settings.tensionSensitivity()),
            new TensionCalculator(settings.contradictionMultiplier()),
            settings
        );
        state = new PlayerState(UUID.randomUUID(), "Bench");
        evidenceBatch = new ArrayList<>();
        WorldPosition pos = WorldPosition.of("world", 0, 64, 0);
        for (int i = 0; i < 16; i++) {
            evidenceBatch.add(new Evidence(
                EvidenceId.generate(),
                state.uuid(),
                EvidenceKind.IMPOSSIBLE_ACCELERATION,
                ConfidenceDimension.MOVEMENT,
                0.5 + (i * 0.01),
                0.9,
                Instant.now(),
                100 + i,
                pos,
                "benchmark evidence " + i,
                "bench-check",
                java.util.Map.of(),
                java.util.Set.of()
            ));
        }
    }

    @Benchmark
    public void propagateBatch() {
        engine.propagate(state, evidenceBatch);
    }
}
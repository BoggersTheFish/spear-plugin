package com.badgersmc.tsspear.application.stress;

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
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphPropagationStressTest {
    @Test
    void propagatesEvidenceForFiveHundredPlayersUnderTwoSeconds() {
        TSSpearSettings settings = TSSpearSettings.defaults();
        PoolRegistry.install(settings);
        GraphEngine engine = new GraphEngine(
            new ConfidenceResolver(settings.tensionSensitivity()),
            new TensionCalculator(settings.contradictionMultiplier()),
            settings
        );
        WorldPosition pos = WorldPosition.of("world", 0, 64, 0);
        long start = System.nanoTime();
        for (int p = 0; p < 500; p++) {
            PlayerState state = new PlayerState(UUID.randomUUID(), "P" + p);
            List<Evidence> batch = new ArrayList<>();
            for (int e = 0; e < 4; e++) {
                batch.add(new Evidence(
                    EvidenceId.generate(),
                    state.uuid(),
                    EvidenceKind.SPEED_VIOLATION,
                    ConfidenceDimension.MOVEMENT,
                    0.4,
                    0.8,
                    Instant.now(),
                    e,
                    pos,
                    "stress",
                    "stress-check",
                    java.util.Map.of(),
                    java.util.Set.of()
                ));
            }
            engine.propagate(state, batch);
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMs < 10_000, "500-player propagation took " + elapsedMs + "ms");
    }
}
package com.badgersmc.tsspear.check.movement;

import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.check.CheckConfig;
import com.badgersmc.tsspear.domain.check.CheckContext;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.model.sample.MovementSample;
import com.badgersmc.tsspear.domain.model.world.Vector3;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImpossibleMovementCheckTest {
    @Test
    void emitsEvidenceWhenAccelerationExceedsLimit() {
        UUID playerId = UUID.randomUUID();
        PlayerState state = new PlayerState(playerId, "Test");
        WorldPosition world = WorldPosition.of("world", 0, 64, 0);

        MovementSample previous = new MovementSample(
            playerId, 100, Instant.now(), world, world,
            Vector3.zero(), true, false, false, false, false, 0, 50, "SURVIVAL"
        );
        MovementSample current = new MovementSample(
            playerId, 101, Instant.now(),
            world,
            WorldPosition.of("world", 5, 64, 0),
            new Vector3(5, 0, 0),
            true, false, false, false, false, 0, 50, "SURVIVAL"
        );
        state.movement().recordSample(previous);

        CheckConfig config = new CheckConfig();
        config.setDouble("max-acceleration", 0.52);
        CheckContext ctx = CheckContext.forMovement(state, current, config, Instant.now());

        List<Evidence> result = new ImpossibleMovementCheck().evaluate(ctx);
        assertEquals(1, result.size());
        assertEquals(EvidenceKind.IMPOSSIBLE_ACCELERATION, result.get(0).kind());
        assertTrue(result.get(0).severity() > 0);
    }

    @Test
    void noEvidenceWhenWithinLimit() {
        UUID playerId = UUID.randomUUID();
        PlayerState state = new PlayerState(playerId, "Test");
        WorldPosition world = WorldPosition.of("world", 0, 64, 0);

        MovementSample previous = new MovementSample(
            playerId, 100, Instant.now(), world, world,
            Vector3.zero(), true, false, false, false, false, 0, 50, "SURVIVAL"
        );
        MovementSample current = new MovementSample(
            playerId, 101, Instant.now(),
            world,
            WorldPosition.of("world", 0.1, 64, 0),
            new Vector3(0.1, 0, 0),
            true, false, false, false, false, 0, 50, "SURVIVAL"
        );
        state.movement().recordSample(previous);

        CheckConfig config = new CheckConfig();
        config.setDouble("max-acceleration", 0.52);
        CheckContext ctx = CheckContext.forMovement(state, current, config, Instant.now());

        List<Evidence> result = new ImpossibleMovementCheck().evaluate(ctx);
        assertTrue(result.isEmpty());
    }
}
package com.badgersmc.tsspear.application.engine;

import com.badgersmc.tsspear.application.bus.EventBus;
import com.badgersmc.tsspear.application.bus.PlayerSampleEvent;
import com.badgersmc.tsspear.domain.model.replay.ReplayTriggerReason;
import com.badgersmc.tsspear.domain.model.sample.MovementSample;
import com.badgersmc.tsspear.domain.model.world.Vector3;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;
import com.badgersmc.tsspear.infrastructure.memory.InMemoryReplayStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplayEngineTest {
    @Test
    void capturesMovementInRingBuffer() {
        ReplayEngine engine = new ReplayEngine(new InMemoryReplayStore(), 30, 20, 300);
        EventBus bus = new EventBus();
        engine.start(bus);

        UUID playerId = UUID.randomUUID();
        MovementSample sample = new MovementSample(
            playerId, 1, Instant.now(),
            WorldPosition.of("world", 0, 64, 0),
            WorldPosition.of("world", 1, 64, 0),
            new Vector3(1, 0, 0),
            true, false, false, false, false, 0, 50, "SURVIVAL"
        );
        bus.publish(new PlayerSampleEvent(sample, Instant.now()));

        assertEquals(1, engine.liveBuffer(playerId).size());
        var sequence = engine.snapshot(playerId, "Test", ReplayTriggerReason.STAFF_REQUEST);
        assertTrue(sequence.isPresent());
        assertEquals(1, sequence.get().frames().size());
    }
}
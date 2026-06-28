package com.badgersmc.tsspear.application.service;

import com.badgersmc.tsspear.application.engine.ReplayEngine;
import com.badgersmc.tsspear.infrastructure.memory.InMemoryReplayStore;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FreezeServiceTest {
    @Test
    void freezeEnablesContinuousCapture() {
        ReplayEngine replay = new ReplayEngine(new InMemoryReplayStore(), 30, 20, 300);
        FreezeService freeze = new FreezeService(replay);
        UUID playerId = UUID.randomUUID();

        assertTrue(freeze.freeze(playerId, "Steve"));
        assertTrue(freeze.isFrozen(playerId));
        assertTrue(replay.isContinuousCapture(playerId));

        assertFalse(freeze.freeze(playerId, "Steve"));
        assertTrue(freeze.unfreeze(playerId));
        assertFalse(replay.isContinuousCapture(playerId));
    }

    @Test
    void unfreezeDisablesContinuousCapture() {
        ReplayEngine replay = new ReplayEngine(new InMemoryReplayStore(), 30, 20, 300);
        FreezeService freeze = new FreezeService(replay);
        UUID playerId = UUID.randomUUID();
        freeze.freeze(playerId, "Steve");
        freeze.unfreeze(playerId);
        assertFalse(replay.isContinuousCapture(playerId));
    }
}
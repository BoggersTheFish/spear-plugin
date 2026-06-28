package com.badgersmc.tsspear.application.service;

import com.badgersmc.tsspear.application.engine.ReplayEngine;
import com.badgersmc.tsspear.domain.model.replay.ReplayTriggerReason;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FreezeService {
    private final ReplayEngine replayEngine;
    private final Set<UUID> frozen = ConcurrentHashMap.newKeySet();
    private final Map<UUID, String> frozenNames = new ConcurrentHashMap<>();

    public FreezeService(ReplayEngine replayEngine) {
        this.replayEngine = replayEngine;
    }

    public boolean freeze(UUID playerId, String playerName) {
        if (!frozen.add(playerId)) {
            return false;
        }
        frozenNames.put(playerId, playerName);
        replayEngine.enableContinuousCapture(playerId);
        replayEngine.snapshot(playerId, playerName, ReplayTriggerReason.FREEZE);
        return true;
    }

    public boolean unfreeze(UUID playerId) {
        if (!frozen.remove(playerId)) {
            return false;
        }
        frozenNames.remove(playerId);
        replayEngine.disableContinuousCapture(playerId);
        return true;
    }

    public boolean isFrozen(UUID playerId) {
        return frozen.contains(playerId);
    }

    public Set<UUID> frozenPlayers() {
        return Set.copyOf(frozen);
    }

    public String frozenName(UUID playerId) {
        return frozenNames.get(playerId);
    }
}
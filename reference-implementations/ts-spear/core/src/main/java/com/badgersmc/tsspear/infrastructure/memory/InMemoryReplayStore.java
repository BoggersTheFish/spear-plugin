package com.badgersmc.tsspear.infrastructure.memory;

import com.badgersmc.tsspear.domain.model.replay.ReplaySequence;
import com.badgersmc.tsspear.domain.port.ReplayStore;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryReplayStore implements ReplayStore {
    private final Map<UUID, ReplaySequence> byId = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> latestByPlayer = new ConcurrentHashMap<>();

    @Override
    public void store(ReplaySequence sequence) {
        byId.put(sequence.sequenceId(), sequence);
        latestByPlayer.put(sequence.playerId(), sequence.sequenceId());
    }

    @Override
    public Optional<ReplaySequence> findById(UUID sequenceId) {
        return Optional.ofNullable(byId.get(sequenceId));
    }

    @Override
    public Optional<ReplaySequence> findLatestForPlayer(UUID playerId) {
        UUID id = latestByPlayer.get(playerId);
        return id == null ? Optional.empty() : findById(id);
    }
}
package com.badgersmc.tsspear.infrastructure.memory;

import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.port.PlayerStateRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryPlayerStateRepository implements PlayerStateRepository {
    private final Map<UUID, PlayerState> store = new ConcurrentHashMap<>();

    @Override
    public Optional<PlayerState> findById(UUID playerId) {
        return Optional.ofNullable(store.get(playerId));
    }

    @Override
    public void save(PlayerState state) {
        store.put(state.uuid(), state.snapshot());
    }

    @Override
    public void delete(UUID playerId) {
        store.remove(playerId);
    }
}
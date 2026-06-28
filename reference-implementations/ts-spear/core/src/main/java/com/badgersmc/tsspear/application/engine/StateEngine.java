package com.badgersmc.tsspear.application.engine;

import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.port.PlayerStateRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StateEngine {
    private final Map<UUID, PlayerState> active = new ConcurrentHashMap<>();
    private final PlayerStateRepository repository;

    public StateEngine(PlayerStateRepository repository) {
        this.repository = repository;
    }

    public PlayerState getOrCreate(UUID playerId, String name) {
        return active.computeIfAbsent(playerId, id -> {
            Optional<PlayerState> stored = repository.findById(id);
            return stored.orElseGet(() -> new PlayerState(id, name));
        });
    }

    public Optional<PlayerState> get(UUID playerId) {
        return Optional.ofNullable(active.get(playerId));
    }

    public void remove(UUID playerId) {
        PlayerState state = active.remove(playerId);
        if (state != null) {
            repository.save(state);
        }
    }

    public void persistAll() {
        for (PlayerState state : active.values()) {
            repository.save(state);
        }
    }

    public java.util.Collection<PlayerState> activePlayers() {
        return java.util.List.copyOf(active.values());
    }
}
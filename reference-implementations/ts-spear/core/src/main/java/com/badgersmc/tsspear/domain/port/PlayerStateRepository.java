package com.badgersmc.tsspear.domain.port;

import com.badgersmc.tsspear.domain.model.player.PlayerState;

import java.util.Optional;
import java.util.UUID;

public interface PlayerStateRepository {
    Optional<PlayerState> findById(UUID playerId);

    void save(PlayerState state);

    void delete(UUID playerId);
}
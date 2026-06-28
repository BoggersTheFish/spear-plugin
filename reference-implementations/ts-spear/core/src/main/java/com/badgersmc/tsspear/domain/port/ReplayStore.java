package com.badgersmc.tsspear.domain.port;

import com.badgersmc.tsspear.domain.model.replay.ReplaySequence;

import java.util.Optional;
import java.util.UUID;

public interface ReplayStore {
    void store(ReplaySequence sequence);

    Optional<ReplaySequence> findById(UUID sequenceId);

    Optional<ReplaySequence> findLatestForPlayer(UUID playerId);
}
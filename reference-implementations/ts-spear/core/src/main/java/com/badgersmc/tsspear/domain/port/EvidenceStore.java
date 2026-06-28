package com.badgersmc.tsspear.domain.port;

import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.EvidenceId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvidenceStore {
    void store(Evidence evidence);

    List<Evidence> findByPlayer(UUID playerId, int limit);

    Optional<Evidence> findById(EvidenceId id);
}
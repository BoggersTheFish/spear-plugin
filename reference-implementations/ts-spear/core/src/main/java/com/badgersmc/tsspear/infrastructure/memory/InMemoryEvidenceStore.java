package com.badgersmc.tsspear.infrastructure.memory;

import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.EvidenceId;
import com.badgersmc.tsspear.domain.port.EvidenceStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryEvidenceStore implements EvidenceStore {
    private final Map<UUID, Evidence> byId = new ConcurrentHashMap<>();
    private final Map<UUID, List<EvidenceId>> byPlayer = new ConcurrentHashMap<>();

    @Override
    public void store(Evidence evidence) {
        byId.put(evidence.id().value(), evidence);
        byPlayer.computeIfAbsent(evidence.playerId(), k -> new ArrayList<>()).add(evidence.id());
    }

    @Override
    public List<Evidence> findByPlayer(UUID playerId, int limit) {
        List<EvidenceId> ids = byPlayer.getOrDefault(playerId, List.of());
        return ids.stream()
            .map(id -> byId.get(id.value()))
            .filter(e -> e != null)
            .limit(limit)
            .toList();
    }

    @Override
    public Optional<Evidence> findById(EvidenceId id) {
        return Optional.ofNullable(byId.get(id.value()));
    }
}
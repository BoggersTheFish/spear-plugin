package com.badgersmc.tsspear.infrastructure.memory;

import com.badgersmc.tsspear.domain.model.annotation.StaffAnnotation;
import com.badgersmc.tsspear.domain.port.AnnotationStore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryAnnotationStore implements AnnotationStore {
    private final Map<UUID, List<StaffAnnotation>> byPlayer = new ConcurrentHashMap<>();

    @Override
    public void store(StaffAnnotation annotation) {
        byPlayer.computeIfAbsent(annotation.playerId(), id -> new ArrayList<>()).add(annotation);
    }

    @Override
    public Optional<StaffAnnotation> findLatestForPlayer(UUID playerId) {
        return findByPlayer(playerId, 1).stream().findFirst();
    }

    @Override
    public List<StaffAnnotation> findByPlayer(UUID playerId, int limit) {
        List<StaffAnnotation> list = byPlayer.getOrDefault(playerId, List.of());
        return list.stream()
            .sorted(Comparator.comparing(StaffAnnotation::annotatedAt).reversed())
            .limit(Math.max(0, limit))
            .toList();
    }
}
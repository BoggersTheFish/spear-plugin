package com.badgersmc.tsspear.domain.port;

import com.badgersmc.tsspear.domain.model.annotation.StaffAnnotation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnnotationStore {
    void store(StaffAnnotation annotation);

    Optional<StaffAnnotation> findLatestForPlayer(UUID playerId);

    List<StaffAnnotation> findByPlayer(UUID playerId, int limit);
}
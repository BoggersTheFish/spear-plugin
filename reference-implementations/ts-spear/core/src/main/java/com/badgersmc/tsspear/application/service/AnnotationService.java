package com.badgersmc.tsspear.application.service;

import com.badgersmc.tsspear.api.ml.AnnotationVerdict;
import com.badgersmc.tsspear.domain.model.annotation.StaffAnnotation;
import com.badgersmc.tsspear.domain.port.AnnotationStore;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class AnnotationService {
    private final AnnotationStore annotationStore;

    public AnnotationService(AnnotationStore annotationStore) {
        this.annotationStore = annotationStore;
    }

    public StaffAnnotation annotate(
        UUID playerId,
        String staffName,
        AnnotationVerdict verdict,
        String notes,
        UUID linkedExportId
    ) {
        StaffAnnotation annotation = new StaffAnnotation(
            UUID.randomUUID(),
            playerId,
            staffName,
            verdict,
            notes,
            Instant.now(),
            linkedExportId
        );
        annotationStore.store(annotation);
        return annotation;
    }

    public Optional<StaffAnnotation> latestForPlayer(UUID playerId) {
        return annotationStore.findLatestForPlayer(playerId);
    }

    public List<StaffAnnotation> historyForPlayer(UUID playerId, int limit) {
        return annotationStore.findByPlayer(playerId, limit);
    }

    public Optional<Map<String, Object>> labelForExport(UUID playerId) {
        return latestForPlayer(playerId).map(StaffAnnotation::toLabelMap);
    }
}
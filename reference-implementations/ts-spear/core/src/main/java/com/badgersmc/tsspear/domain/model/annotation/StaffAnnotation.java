package com.badgersmc.tsspear.domain.model.annotation;

import com.badgersmc.tsspear.api.ml.AnnotationVerdict;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record StaffAnnotation(
    UUID annotationId,
    UUID playerId,
    String staffName,
    AnnotationVerdict verdict,
    String notes,
    Instant annotatedAt,
    UUID linkedExportId
) {
    public Map<String, Object> toLabelMap() {
        return Map.of(
            "verdict", verdict.name(),
            "annotatedBy", staffName,
            "annotatedAt", annotatedAt.toString(),
            "notes", notes == null ? "" : notes
        );
    }
}
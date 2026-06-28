package com.badgersmc.tsspear.domain.model.graph;

import com.badgersmc.tsspear.api.graph.EdgeKind;

import java.time.Instant;
import java.util.Optional;

public record GraphEdge(
    String edgeId,
    String fromNodeId,
    String toNodeId,
    EdgeKind kind,
    double strength,
    Instant createdAt,
    Optional<Instant> expiresAt,
    String reason
) {
    public GraphEdge {
        if (strength < 0.0 || strength > 1.0) {
            throw new IllegalArgumentException("strength must be 0..1");
        }
        expiresAt = expiresAt == null ? Optional.empty() : expiresAt;
    }
}
package com.badgersmc.tsspear.domain.model.evidence;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.model.ids.EvidenceId;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record Evidence(
    EvidenceId id,
    UUID playerId,
    EvidenceKind kind,
    ConfidenceDimension dimension,
    double severity,
    double credibility,
    Instant timestamp,
    long tick,
    WorldPosition location,
    String reason,
    String sourceCheckId,
    Map<String, Object> payload,
    Set<EvidenceId> relatedEvidence
) {
    public Evidence {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(reason, "reason");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        Objects.requireNonNull(sourceCheckId, "sourceCheckId");
        payload = payload == null ? Map.of() : Map.copyOf(payload);
        relatedEvidence = relatedEvidence == null ? Set.of() : Set.copyOf(relatedEvidence);
        severity = clamp(severity);
        credibility = clamp(credibility);
    }

    public double weight() {
        return severity * credibility;
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private EvidenceId id = EvidenceId.generate();
        private UUID playerId;
        private EvidenceKind kind;
        private ConfidenceDimension dimension;
        private double severity;
        private double credibility = 1.0;
        private Instant timestamp = Instant.now();
        private long tick;
        private WorldPosition location;
        private String reason;
        private String sourceCheckId;
        private Map<String, Object> payload = Map.of();
        private Set<EvidenceId> relatedEvidence = Set.of();

        public Builder id(EvidenceId id) {
            this.id = id;
            return this;
        }

        public Builder playerId(UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        public Builder kind(EvidenceKind kind) {
            this.kind = kind;
            return this;
        }

        public Builder dimension(ConfidenceDimension dimension) {
            this.dimension = dimension;
            return this;
        }

        public Builder severity(double severity) {
            this.severity = severity;
            return this;
        }

        public Builder credibility(double credibility) {
            this.credibility = credibility;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder tick(long tick) {
            this.tick = tick;
            return this;
        }

        public Builder location(WorldPosition location) {
            this.location = location;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder sourceCheckId(String sourceCheckId) {
            this.sourceCheckId = sourceCheckId;
            return this;
        }

        public Builder payload(Map<String, Object> payload) {
            this.payload = payload;
            return this;
        }

        public Builder relatedEvidence(Set<EvidenceId> relatedEvidence) {
            this.relatedEvidence = relatedEvidence;
            return this;
        }

        public Evidence build() {
            return new Evidence(
                id, playerId, kind, dimension, severity, credibility,
                timestamp, tick, location, reason, sourceCheckId, payload, relatedEvidence
            );
        }

        public void reset() {
            id = EvidenceId.generate();
            playerId = null;
            kind = null;
            dimension = null;
            severity = 0.0;
            credibility = 1.0;
            timestamp = Instant.now();
            tick = 0L;
            location = null;
            reason = null;
            sourceCheckId = null;
            payload = Map.of();
            relatedEvidence = Set.of();
        }
    }
}
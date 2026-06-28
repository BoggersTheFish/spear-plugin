package com.badgersmc.tsspear.domain.model.receipt;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.domain.model.ids.EvidenceId;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ConfidenceReceipt(
    UUID receiptId,
    UUID playerId,
    Instant time,
    String world,
    WorldPosition location,
    String event,
    ConfidenceDimension dimension,
    double confidenceDelta,
    double previousValue,
    double newValue,
    String reason,
    List<EvidenceId> linkedEvidenceIds,
    long stateVersion,
    double tensionBefore,
    double tensionAfter,
    int supportingCount,
    int conflictingCount
) {
    public ConfidenceReceipt {
        Objects.requireNonNull(receiptId, "receiptId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(reason, "reason");
        linkedEvidenceIds = linkedEvidenceIds == null ? List.of() : List.copyOf(linkedEvidenceIds);
    }

    public static ConfidenceReceipt create(
        UUID playerId,
        String event,
        ConfidenceDimension dimension,
        double previousValue,
        double newValue,
        String reason,
        List<EvidenceId> linkedEvidenceIds,
        long stateVersion,
        double tensionBefore,
        double tensionAfter,
        int supportingCount,
        int conflictingCount,
        WorldPosition location
    ) {
        String world = location == null ? null : location.world();
        return new ConfidenceReceipt(
            UUID.randomUUID(),
            playerId,
            Instant.now(),
            world,
            location,
            event,
            dimension,
            newValue - previousValue,
            previousValue,
            newValue,
            reason,
            linkedEvidenceIds,
            stateVersion,
            tensionBefore,
            tensionAfter,
            supportingCount,
            conflictingCount
        );
    }
}
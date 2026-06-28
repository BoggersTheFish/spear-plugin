package com.badgersmc.tsspear.application.ml;

import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record EvidenceExportBatch(
    UUID exportId,
    UUID playerId,
    Instant exportedAt,
    Instant windowStart,
    Instant windowEnd,
    Map<String, Double> confidenceVector,
    Map<String, Object> evidenceGraph,
    float[][] movementSequence,
    float[][] combatSequence,
    List<ConfidenceReceipt> receipts,
    Map<String, Object> label
) {}
package com.badgersmc.tsspear.application.ml;

import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Schema v1 export document aligned with docs/schemas/export.schema.json */
public record MlExportDocument(
    int schemaVersion,
    UUID exportId,
    Instant exportedAt,
    UUID playerId,
    TimeWindow timeWindow,
    Map<String, Double> confidenceVector,
    Map<String, Object> evidenceGraph,
    float[][] movementSequence,
    float[][] combatSequence,
    float[][] packetSequence,
    float[] features,
    List<String> featureNames,
    List<ConfidenceReceipt> receipts,
    Map<String, Object> label
) {}
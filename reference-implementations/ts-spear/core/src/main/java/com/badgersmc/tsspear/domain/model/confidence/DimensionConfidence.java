package com.badgersmc.tsspear.domain.model.confidence;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.domain.model.ids.EvidenceId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DimensionConfidence {
    private final ConfidenceDimension dimension;
    private double confidence;
    private double rawActivation;
    private double tension;
    private Instant timestamp;
    private Instant lastEvidenceAt;
    private DecayCurve decayCurve;
    private double decayRate;
    private String explanation;
    private final List<EvidenceId> supportingEvidence;
    private final List<EvidenceId> conflictingEvidence;
    private final List<UUID> receipts;

    public DimensionConfidence(ConfidenceDimension dimension) {
        this.dimension = dimension;
        this.confidence = 0.0;
        this.rawActivation = 0.0;
        this.tension = 0.0;
        this.timestamp = Instant.now();
        this.lastEvidenceAt = Instant.EPOCH;
        this.decayCurve = DecayCurve.EXPONENTIAL;
        this.decayRate = 0.02;
        this.explanation = "No evidence";
        this.supportingEvidence = new ArrayList<>();
        this.conflictingEvidence = new ArrayList<>();
        this.receipts = new ArrayList<>();
    }

    public ConfidenceDimension dimension() {
        return dimension;
    }

    public double confidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = clamp(confidence);
        this.timestamp = Instant.now();
    }

    public double rawActivation() {
        return rawActivation;
    }

    public void setRawActivation(double rawActivation) {
        this.rawActivation = clamp(rawActivation);
    }

    public double tension() {
        return tension;
    }

    public void setTension(double tension) {
        this.tension = Math.max(0.0, tension);
    }

    public Instant timestamp() {
        return timestamp;
    }

    public Instant lastEvidenceAt() {
        return lastEvidenceAt;
    }

    public void touchEvidence(Instant at) {
        this.lastEvidenceAt = at;
    }

    public DecayCurve decayCurve() {
        return decayCurve;
    }

    public void setDecayCurve(DecayCurve decayCurve) {
        this.decayCurve = decayCurve;
    }

    public double decayRate() {
        return decayRate;
    }

    public void setDecayRate(double decayRate) {
        this.decayRate = decayRate;
    }

    public String explanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<EvidenceId> supportingEvidence() {
        return List.copyOf(supportingEvidence);
    }

    public List<EvidenceId> conflictingEvidence() {
        return List.copyOf(conflictingEvidence);
    }

    public List<UUID> receipts() {
        return List.copyOf(receipts);
    }

    public void addSupporting(EvidenceId id) {
        if (!supportingEvidence.contains(id)) {
            supportingEvidence.add(id);
        }
    }

    public void addConflicting(EvidenceId id) {
        if (!conflictingEvidence.contains(id)) {
            conflictingEvidence.add(id);
        }
    }

    public void addReceipt(UUID receiptId) {
        receipts.add(receiptId);
    }

    public DimensionConfidence snapshot() {
        DimensionConfidence copy = new DimensionConfidence(dimension);
        copy.confidence = confidence;
        copy.rawActivation = rawActivation;
        copy.tension = tension;
        copy.timestamp = timestamp;
        copy.lastEvidenceAt = lastEvidenceAt;
        copy.decayCurve = decayCurve;
        copy.decayRate = decayRate;
        copy.explanation = explanation;
        copy.supportingEvidence.addAll(supportingEvidence);
        copy.conflictingEvidence.addAll(conflictingEvidence);
        copy.receipts.addAll(receipts);
        return copy;
    }

    public static Map<ConfidenceDimension, DimensionConfidence> emptyVector() {
        Map<ConfidenceDimension, DimensionConfidence> map = new EnumMap<>(ConfidenceDimension.class);
        for (ConfidenceDimension dim : ConfidenceDimension.values()) {
            if (dim != ConfidenceDimension.AGGREGATE) {
                map.put(dim, new DimensionConfidence(dim));
            }
        }
        return map;
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
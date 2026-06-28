package com.badgersmc.tsspear.domain.model.graph;

import com.badgersmc.tsspear.api.graph.GraphKind;
import com.badgersmc.tsspear.domain.model.ids.EvidenceId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class GraphNode {
    private String nodeId;
    private GraphKind graphKind;
    private String category;
    private double activation;
    private double confidence;
    private double weight;
    private Instant timestamp;
    private String reason;
    private final List<EvidenceId> supportingEvidence;
    private final List<EvidenceId> conflictingEvidence;
    private double decayRate;
    private final Set<String> dependencies;
    private final List<UUID> receipts;

    public GraphNode(String nodeId, GraphKind graphKind, String category, String reason) {
        this.nodeId = nodeId;
        this.graphKind = graphKind;
        this.category = category;
        this.reason = reason;
        this.timestamp = Instant.now();
        this.supportingEvidence = new ArrayList<>();
        this.conflictingEvidence = new ArrayList<>();
        this.dependencies = new HashSet<>();
        this.receipts = new ArrayList<>();
    }

    public String nodeId() {
        return nodeId;
    }

    public GraphKind graphKind() {
        return graphKind;
    }

    public String category() {
        return category;
    }

    public double activation() {
        return activation;
    }

    public void setActivation(double activation) {
        this.activation = Math.max(0.0, Math.min(1.0, activation));
    }

    public double confidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
    }

    public double weight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public void touch() {
        this.timestamp = Instant.now();
    }

    public String reason() {
        return reason;
    }

    public List<EvidenceId> supportingEvidence() {
        return List.copyOf(supportingEvidence);
    }

    public List<EvidenceId> conflictingEvidence() {
        return List.copyOf(conflictingEvidence);
    }

    public double decayRate() {
        return decayRate;
    }

    public void setDecayRate(double decayRate) {
        this.decayRate = decayRate;
    }

    public Set<String> dependencies() {
        return Set.copyOf(dependencies);
    }

    public List<UUID> receipts() {
        return List.copyOf(receipts);
    }

    public void addSupporting(EvidenceId id) {
        supportingEvidence.add(id);
    }

    public void addConflicting(EvidenceId id) {
        conflictingEvidence.add(id);
    }

    public void addDependency(String nodeId) {
        dependencies.add(nodeId);
    }

    public void addReceipt(UUID receiptId) {
        receipts.add(receiptId);
    }

    public void reinitialize(String nodeId, GraphKind graphKind, String category, String reason) {
        this.nodeId = nodeId;
        this.graphKind = graphKind;
        this.category = category;
        this.reason = reason;
        reset();
    }

    public void reset() {
        activation = 0.0;
        confidence = 0.0;
        weight = 0.0;
        timestamp = Instant.now();
        decayRate = 0.0;
        supportingEvidence.clear();
        conflictingEvidence.clear();
        dependencies.clear();
        receipts.clear();
    }
}
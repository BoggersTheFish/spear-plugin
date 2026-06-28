package com.badgersmc.tsspear.application.engine;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.api.graph.EdgeKind;
import com.badgersmc.tsspear.api.graph.GraphKind;
import com.badgersmc.tsspear.application.config.TSSpearSettings;
import com.badgersmc.tsspear.domain.model.confidence.ConfidenceVector;
import com.badgersmc.tsspear.domain.model.confidence.DimensionConfidence;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.graph.GraphEdge;
import com.badgersmc.tsspear.domain.model.graph.GraphNode;
import com.badgersmc.tsspear.domain.model.graph.PlayerGraph;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;
import com.badgersmc.tsspear.domain.service.ConfidenceResolver;
import com.badgersmc.tsspear.domain.service.TensionCalculator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class GraphEngine {
    private final ConfidenceResolver resolver;
    private final TensionCalculator tensionCalculator;
    private final TSSpearSettings settings;

    public GraphEngine(ConfidenceResolver resolver, TensionCalculator tensionCalculator, TSSpearSettings settings) {
        this.resolver = resolver;
        this.tensionCalculator = tensionCalculator;
        this.settings = settings;
    }

    public PropagationResult propagate(PlayerState state, List<Evidence> evidenceList) {
        return propagate(state, evidenceList, null);
    }

    public PropagationResult propagate(PlayerState state, List<Evidence> evidenceList,
                                       com.badgersmc.tsspear.application.metrics.PerformanceMetrics metrics) {
        long start = System.nanoTime();
        List<ConfidenceReceipt> receipts = new ArrayList<>();
        int batchSize = Math.max(1, settings.graphPropagationBatchSize());

        for (int offset = 0; offset < evidenceList.size(); offset += batchSize) {
            int end = Math.min(offset + batchSize, evidenceList.size());
            for (int i = offset; i < end; i++) {
                Evidence evidence = evidenceList.get(i);
                state.addEvidence(evidence);
                insertEvidenceNode(state, evidence);
                applyConstraintTemplates(state, evidence);
            }
            for (ConfidenceDimension dimension : ConfidenceDimension.values()) {
                if (dimension == ConfidenceDimension.AGGREGATE) {
                    continue;
                }
                ConfidenceReceipt receipt = updateDimension(state, dimension);
                if (receipt != null) {
                    receipts.add(receipt);
                }
            }
        }

        double aggregate = state.trust().vector().aggregate();
        state.suspicion().update(aggregate);
        state.trust().setVector(state.trust().vector());
        if (metrics != null) {
            metrics.recordGraphPropagation(System.nanoTime() - start);
        }
        return new PropagationResult(receipts);
    }

    private void insertEvidenceNode(PlayerState state, Evidence evidence) {
        PlayerGraph graph = state.graph(GraphKind.EVIDENCE);
        String nodeId = "ev:" + evidence.id().value();
        GraphNode node = graph.getOrCreate(nodeId, evidence.kind().name(), evidence.reason());
        node.setActivation(evidence.severity());
        node.setConfidence(evidence.weight());
        node.setWeight(evidence.weight());
        node.addSupporting(evidence.id());
        node.touch();
    }

    private void applyConstraintTemplates(PlayerState state, Evidence evidence) {
        PlayerGraph tension = state.graph(GraphKind.TENSION);
        String dimNode = evidence.dimension().name() + "_HIGH";
        GraphNode target = tension.getOrCreate(dimNode, evidence.dimension().name(), "Aggregated " + evidence.dimension());

        GraphEdge supportEdge = new GraphEdge(
            UUID.randomUUID().toString(),
            "ev:" + evidence.id().value(),
            dimNode,
            EdgeKind.SUPPORTS,
            evidence.weight(),
            Instant.now(),
            java.util.Optional.empty(),
            evidence.reason()
        );
        tension.addEdge(supportEdge);
        target.setActivation(Math.min(1.0, target.activation() + evidence.weight() * 0.25));
        target.addSupporting(evidence.id());
        target.touch();

        if (isContradictionKind(evidence.kind())) {
            GraphEdge contradict = new GraphEdge(
                UUID.randomUUID().toString(),
                "ev:" + evidence.id().value(),
                dimNode,
                EdgeKind.CONTRADICTS,
                evidence.weight() * settings.contradictionMultiplier(),
                Instant.now(),
                java.util.Optional.empty(),
                evidence.reason()
            );
            tension.addEdge(contradict);
            DimensionConfidence dim = state.trust().vector().get(evidence.dimension());
            dim.addConflicting(evidence.id());
        } else {
            DimensionConfidence dim = state.trust().vector().get(evidence.dimension());
            dim.addSupporting(evidence.id());
            dim.touchEvidence(evidence.timestamp());
        }
    }

    private boolean isContradictionKind(EvidenceKind kind) {
        return kind == EvidenceKind.LAG_SPIKE || kind == EvidenceKind.TELEPORT;
    }

    private ConfidenceReceipt updateDimension(PlayerState state, ConfidenceDimension dimension) {
        DimensionConfidence dim = state.trust().vector().get(dimension);
        PlayerGraph tensionGraph = state.graph(GraphKind.TENSION);
        String nodeId = dimension.name() + "_HIGH";

        double activation = tensionGraph.find(nodeId).map(GraphNode::activation).orElse(dim.rawActivation());
        double tension = tensionCalculator.compute(dim, tensionGraph, dimension);
        double previous = dim.confidence();
        double resolved = resolver.resolve(activation, tension);

        dim.setRawActivation(activation);
        dim.setTension(tension);
        dim.setConfidence(resolved);
        dim.setExplanation(buildExplanation(dim));

        double delta = Math.abs(resolved - previous);
        if (delta < settings.confidenceEpsilon()) {
            return null;
        }

        ConfidenceReceipt receipt = ConfidenceReceipt.create(
            state.uuid(),
            "GRAPH_PROPAGATION",
            dimension,
            previous,
            resolved,
            dim.explanation(),
            dim.supportingEvidence(),
            state.version(),
            dim.tension(),
            tension,
            dim.supportingEvidence().size(),
            dim.conflictingEvidence().size(),
            state.movement().lastSample() == null ? null : state.movement().lastSample().position()
        );
        dim.addReceipt(receipt.receiptId());
        state.bumpVersion();
        return receipt;
    }

    private String buildExplanation(DimensionConfidence dim) {
        return "confidence=%.2f tension=%.2f support=%d conflict=%d".formatted(
            dim.confidence(),
            dim.tension(),
            dim.supportingEvidence().size(),
            dim.conflictingEvidence().size()
        );
    }
}
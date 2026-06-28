package com.badgersmc.tsspear.domain.service;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.api.graph.EdgeKind;
import com.badgersmc.tsspear.domain.model.confidence.DimensionConfidence;
import com.badgersmc.tsspear.domain.model.graph.PlayerGraph;

public final class TensionCalculator {
    private final double contradictionMultiplier;

    public TensionCalculator(double contradictionMultiplier) {
        this.contradictionMultiplier = contradictionMultiplier;
    }

    public double compute(DimensionConfidence dim, PlayerGraph tensionGraph, ConfidenceDimension dimension) {
        String nodeId = dimension.name() + "_HIGH";
        double supporting = dim.supportingEvidence().size() > 0
            ? dim.supportingEvidence().stream().mapToDouble(e -> 1.0).sum() / dim.supportingEvidence().size()
            : 0.0;
        double contradicting = tensionGraph.edgesOfKind(EdgeKind.CONTRADICTS).stream()
            .filter(e -> e.toNodeId().equals(nodeId))
            .mapToDouble(com.badgersmc.tsspear.domain.model.graph.GraphEdge::strength)
            .sum();
        return Math.max(0.0, supporting - contradicting * contradictionMultiplier);
    }
}
package com.badgersmc.tsspear.application.ml;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.api.graph.GraphKind;
import com.badgersmc.tsspear.domain.model.graph.GraphNode;
import com.badgersmc.tsspear.domain.model.graph.PlayerGraph;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.model.sample.PacketSample;

import java.util.List;

public final class FeatureExtractor {
    public static final String[] FEATURE_NAMES = {
        "movement_confidence",
        "combat_confidence",
        "inventory_confidence",
        "automation_confidence",
        "networking_confidence",
        "interaction_confidence",
        "suspicion_risk",
        "suspicion_delta",
        "aggregate_confidence",
        "tension_aggregate",
        "tension_graph_nodes",
        "tension_graph_avg_activation",
        "packet_count",
        "packet_avg_delta",
        "packet_max_delta",
        "packet_avg_interval_ms",
        "evidence_recent_count"
    };

    public FeatureVector extract(PlayerState state) {
        return extract(state, TimeWindow.lastSeconds(300));
    }

    public FeatureVector extract(PlayerState state, TimeWindow window) {
        float[] features = new float[FEATURE_NAMES.length];
        int i = 0;
        for (ConfidenceDimension dim : ConfidenceDimension.values()) {
            if (dim == ConfidenceDimension.AGGREGATE) {
                continue;
            }
            features[i++] = (float) state.trust().vector().get(dim).confidence();
        }
        features[i++] = (float) state.suspicion().currentRisk();
        features[i++] = (float) state.suspicion().delta();
        features[i++] = (float) state.trust().vector().aggregate();

        double tensionAggregate = 0.0;
        for (ConfidenceDimension dim : ConfidenceDimension.values()) {
            if (dim == ConfidenceDimension.AGGREGATE) {
                continue;
            }
            tensionAggregate += state.trust().vector().get(dim).tension();
        }
        features[i++] = (float) tensionAggregate;

        PlayerGraph tensionGraph = state.graph(GraphKind.TENSION);
        List<GraphNode> tensionNodes = tensionGraph.nodes();
        features[i++] = tensionNodes.size();
        features[i++] = (float) tensionNodes.stream()
            .mapToDouble(GraphNode::activation)
            .average()
            .orElse(0.0);

        List<PacketSample> packets = state.networking().recentPackets();
        features[i++] = packets.size();
        if (packets.isEmpty()) {
            features[i++] = 0f;
            features[i++] = 0f;
            features[i++] = 0f;
        } else {
            double sumDelta = 0.0;
            double maxDelta = 0.0;
            double sumInterval = 0.0;
            for (PacketSample packet : packets) {
                sumDelta += packet.positionDelta();
                maxDelta = Math.max(maxDelta, packet.positionDelta());
                sumInterval += packet.millisSinceLastPacket();
            }
            features[i++] = (float) (sumDelta / packets.size());
            features[i++] = (float) maxDelta;
            features[i++] = (float) (sumInterval / packets.size());
        }

        features[i] = state.recentEvidence().size();
        return new FeatureVector(features, FEATURE_NAMES);
    }

    public static int indexOf(String name) {
        for (int i = 0; i < FEATURE_NAMES.length; i++) {
            if (FEATURE_NAMES[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }
}
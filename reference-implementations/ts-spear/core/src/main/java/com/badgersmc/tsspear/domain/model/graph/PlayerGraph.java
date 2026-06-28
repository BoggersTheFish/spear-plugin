package com.badgersmc.tsspear.domain.model.graph;

import com.badgersmc.tsspear.api.graph.GraphKind;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PlayerGraph {
    private final GraphKind kind;
    private final Map<String, GraphNode> nodes = new LinkedHashMap<>();
    private final List<GraphEdge> edges = new ArrayList<>();

    public PlayerGraph(GraphKind kind) {
        this.kind = kind;
    }

    public GraphKind kind() {
        return kind;
    }

    public GraphNode getOrCreate(String nodeId, String category, String reason) {
        return nodes.computeIfAbsent(nodeId, id -> createNode(id, category, reason));
    }

    private GraphNode createNode(String nodeId, String category, String reason) {
        var pools = com.badgersmc.tsspear.application.pool.PoolRegistry.get();
        if (pools != null && pools.enabled()) {
            return pools.graphNodes().acquire(nodeId, kind, category, reason);
        }
        return new GraphNode(nodeId, kind, category, reason);
    }

    public Optional<GraphNode> find(String nodeId) {
        return Optional.ofNullable(nodes.get(nodeId));
    }

    public void addEdge(GraphEdge edge) {
        edges.add(edge);
    }

    public List<GraphNode> nodes() {
        return List.copyOf(nodes.values());
    }

    public List<GraphEdge> edges() {
        return List.copyOf(edges);
    }

    public List<GraphEdge> edgesOfKind(com.badgersmc.tsspear.api.graph.EdgeKind edgeKind) {
        return edges.stream().filter(e -> e.kind() == edgeKind).toList();
    }
}
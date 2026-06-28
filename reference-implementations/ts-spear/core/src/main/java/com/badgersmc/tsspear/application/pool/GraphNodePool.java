package com.badgersmc.tsspear.application.pool;

import com.badgersmc.tsspear.api.graph.GraphKind;
import com.badgersmc.tsspear.domain.model.graph.GraphNode;

public final class GraphNodePool {
    private final ObjectPool<GraphNode> pool;

    public GraphNodePool(int maxSize) {
        this.pool = new ObjectPool<>(maxSize, () -> new GraphNode("pool", GraphKind.EVIDENCE, "", ""), GraphNode::reset);
    }

    public GraphNode acquire(String nodeId, GraphKind kind, String category, String reason) {
        GraphNode node = pool.acquire();
        node.reinitialize(nodeId, kind, category, reason);
        return node;
    }

    public void release(GraphNode node) {
        pool.release(node);
    }

    public int size() {
        return pool.size();
    }
}
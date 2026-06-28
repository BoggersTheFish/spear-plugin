package com.badgersmc.tsspear.application.pool;

import com.badgersmc.tsspear.application.config.TSSpearSettings;

public final class PoolRegistry {
    private static volatile PoolRegistry instance;

    private final GraphNodePool graphNodePool;
    private final EvidenceBuilderPool evidenceBuilderPool;
    private final ObjectPool<MovementSampleScratch> movementScratchPool;
    private final boolean enabled;

    public PoolRegistry(TSSpearSettings settings) {
        this.enabled = settings.objectPoolingEnabled();
        int graphPool = settings.graphNodePoolSize();
        int evidencePool = settings.evidenceBuilderPoolSize();
        int movementPool = settings.movementScratchPoolSize();
        this.graphNodePool = new GraphNodePool(graphPool);
        this.evidenceBuilderPool = new EvidenceBuilderPool(evidencePool);
        this.movementScratchPool = new ObjectPool<>(movementPool, MovementSampleScratch::new, MovementSampleScratch::reset);
    }

    public static void install(TSSpearSettings settings) {
        instance = new PoolRegistry(settings);
    }

    public static PoolRegistry get() {
        return instance;
    }

    public boolean enabled() {
        return enabled && instance != null;
    }

    public GraphNodePool graphNodes() {
        return graphNodePool;
    }

    public EvidenceBuilderPool evidenceBuilders() {
        return evidenceBuilderPool;
    }

    public MovementSampleScratch acquireMovementScratch() {
        return movementScratchPool.acquire();
    }

    public void releaseMovementScratch(MovementSampleScratch scratch) {
        movementScratchPool.release(scratch);
    }
}
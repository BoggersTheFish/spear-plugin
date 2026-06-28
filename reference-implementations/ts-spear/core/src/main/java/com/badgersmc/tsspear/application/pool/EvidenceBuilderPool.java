package com.badgersmc.tsspear.application.pool;

import com.badgersmc.tsspear.domain.model.evidence.Evidence;

public final class EvidenceBuilderPool {
    private final ObjectPool<Evidence.Builder> pool;

    public EvidenceBuilderPool(int maxSize) {
        this.pool = new ObjectPool<>(maxSize, Evidence::builder, Evidence.Builder::reset);
    }

    public Evidence.Builder acquire() {
        return pool.acquire();
    }

    public void release(Evidence.Builder builder) {
        pool.release(builder);
    }

    public int size() {
        return pool.size();
    }
}
package com.badgersmc.tsspear.application.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public final class PerformanceMetrics {
    private final LongAdder checkDurationNanos = new LongAdder();
    private final LongAdder checkInvocations = new LongAdder();
    private final LongAdder graphPropagationNanos = new LongAdder();
    private final LongAdder graphPropagationCalls = new LongAdder();
    private final AtomicInteger storageQueueDepth = new AtomicInteger();
    private final AtomicLong storageDroppedTasks = new AtomicLong();
    private final AtomicInteger activeWorkerTasks = new AtomicInteger();

    public void recordCheck(long durationNanos) {
        checkDurationNanos.add(durationNanos);
        checkInvocations.increment();
    }

    public void recordGraphPropagation(long durationNanos) {
        graphPropagationNanos.add(durationNanos);
        graphPropagationCalls.increment();
    }

    public void setStorageQueueDepth(int depth) {
        storageQueueDepth.set(depth);
    }

    public void recordStorageDrop() {
        storageDroppedTasks.incrementAndGet();
    }

    public void workerTaskStarted() {
        activeWorkerTasks.incrementAndGet();
    }

    public void workerTaskFinished() {
        activeWorkerTasks.decrementAndGet();
    }

    public Snapshot snapshot() {
        long checks = checkInvocations.sum();
        long graphs = graphPropagationCalls.sum();
        return new Snapshot(
            checks,
            checks == 0 ? 0 : checkDurationNanos.sum() / checks,
            graphs,
            graphs == 0 ? 0 : graphPropagationNanos.sum() / graphs,
            storageQueueDepth.get(),
            storageDroppedTasks.get(),
            activeWorkerTasks.get()
        );
    }

    public record Snapshot(
        long checkInvocations,
        long avgCheckNanos,
        long graphPropagationCalls,
        long avgGraphPropagationNanos,
        int storageQueueDepth,
        long storageDroppedTasks,
        int activeWorkerTasks
    ) {}
}
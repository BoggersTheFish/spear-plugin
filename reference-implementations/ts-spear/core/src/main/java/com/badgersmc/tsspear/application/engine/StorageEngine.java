package com.badgersmc.tsspear.application.engine;

import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;
import com.badgersmc.tsspear.domain.port.EvidenceStore;
import com.badgersmc.tsspear.domain.port.ReceiptStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class StorageEngine {
    private static final Logger LOGGER = Logger.getLogger(StorageEngine.class.getName());

    private final ReceiptStore receiptStore;
    private final EvidenceStore evidenceStore;
    private final BlockingQueue<StorageTask> queue = new LinkedBlockingQueue<>(10_000);
    private final ExecutorService worker;
    private final com.badgersmc.tsspear.application.metrics.PerformanceMetrics metrics;
    private volatile boolean running;

    public StorageEngine(ReceiptStore receiptStore, EvidenceStore evidenceStore, int flushBatchSize,
                         long flushIntervalMs, com.badgersmc.tsspear.application.metrics.PerformanceMetrics metrics) {
        this.receiptStore = receiptStore;
        this.evidenceStore = evidenceStore;
        this.metrics = metrics;
        this.worker = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ts-spear-storage");
            t.setDaemon(true);
            return t;
        });
        worker.submit(() -> flushLoop(flushBatchSize, flushIntervalMs));
    }

    public void enqueueReceipt(ConfidenceReceipt receipt) {
        offer(new StorageTask(TaskKind.RECEIPT, receipt, null));
    }

    public void enqueueEvidence(Evidence evidence) {
        offer(new StorageTask(TaskKind.EVIDENCE, null, evidence));
    }

    private void offer(StorageTask task) {
        if (!queue.offer(task)) {
            queue.poll();
            queue.offer(task);
            if (metrics != null) {
                metrics.recordStorageDrop();
            }
        }
        if (metrics != null) {
            metrics.setStorageQueueDepth(queue.size());
        }
    }

    public int queueDepth() {
        return queue.size();
    }

    public void flushAndShutdown(long timeoutMs) {
        running = false;
        drainAll();
        worker.shutdown();
        try {
            worker.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void flushLoop(int batchSize, long flushIntervalMs) {
        running = true;
        while (running) {
            try {
                Thread.sleep(flushIntervalMs);
                drainBatch(batchSize);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void drainAll() {
        drainBatch(Integer.MAX_VALUE);
    }

    private void drainBatch(int max) {
        List<StorageTask> batch = new ArrayList<>();
        queue.drainTo(batch, max);
        for (StorageTask task : batch) {
            try {
                switch (task.kind()) {
                    case RECEIPT -> receiptStore.store(task.receipt());
                    case EVIDENCE -> evidenceStore.store(task.evidence());
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Storage write failed", ex);
            }
        }
    }

    private enum TaskKind { RECEIPT, EVIDENCE }

    private record StorageTask(TaskKind kind, ConfidenceReceipt receipt, Evidence evidence) {}
}
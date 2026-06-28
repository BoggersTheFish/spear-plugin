package com.badgersmc.tsspear.application.service;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;
import com.badgersmc.tsspear.domain.port.ReceiptStore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TimelineService {
    private final ReceiptStore receiptStore;

    public TimelineService(ReceiptStore receiptStore) {
        this.receiptStore = receiptStore;
    }

    public TimelineReport build(UUID playerId, int limit) {
        List<ConfidenceReceipt> receipts = new ArrayList<>(receiptStore.findByPlayer(playerId, limit));
        receipts.sort(Comparator.comparing(ConfidenceReceipt::time));
        List<TimelineEntry> entries = receipts.stream()
            .map(r -> new TimelineEntry(
                r.time(),
                r.event(),
                r.dimension(),
                r.previousValue(),
                r.newValue(),
                r.reason()
            ))
            .toList();
        return new TimelineReport(playerId, entries);
    }

    public record TimelineEntry(
        Instant time,
        String event,
        ConfidenceDimension dimension,
        double previous,
        double current,
        String reason
    ) {}

    public record TimelineReport(UUID playerId, List<TimelineEntry> entries) {
        public Map<ConfidenceDimension, Double> latestConfidence() {
            Map<ConfidenceDimension, Double> latest = new EnumMap<>(ConfidenceDimension.class);
            for (TimelineEntry entry : entries) {
                latest.put(entry.dimension(), entry.current());
            }
            return latest;
        }
    }
}
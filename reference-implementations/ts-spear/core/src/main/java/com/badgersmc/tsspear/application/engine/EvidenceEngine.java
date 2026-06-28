package com.badgersmc.tsspear.application.engine;

import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.port.EvidenceStore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EvidenceEngine {
    private final EvidenceStore evidenceStore;
    private final long dedupeWindowMs;
    private final Map<String, Long> recentKeys = new ConcurrentHashMap<>();

    public EvidenceEngine(EvidenceStore evidenceStore, long dedupeWindowMs) {
        this.evidenceStore = evidenceStore;
        this.dedupeWindowMs = dedupeWindowMs;
    }

    public List<Evidence> ingest(List<Evidence> raw) {
        List<Evidence> accepted = new ArrayList<>();
        long now = System.currentTimeMillis();
        prune(now);

        for (Evidence evidence : raw) {
            if (evidence.reason() == null || evidence.reason().isBlank()) {
                continue;
            }
            String key = dedupeKey(evidence);
            Long last = recentKeys.get(key);
            if (last != null && now - last < dedupeWindowMs) {
                continue;
            }
            recentKeys.put(key, now);
            evidenceStore.store(evidence);
            accepted.add(evidence);
        }
        return accepted;
    }

    private static String dedupeKey(Evidence evidence) {
        return evidence.playerId() + ":" + evidence.kind() + ":" + evidence.sourceCheckId();
    }

    private void prune(long now) {
        Iterator<Map.Entry<String, Long>> it = recentKeys.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> entry = it.next();
            if (now - entry.getValue() > dedupeWindowMs * 4) {
                it.remove();
            }
        }
    }
}
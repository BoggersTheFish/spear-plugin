package com.badgersmc.tsspear.infrastructure.memory;

import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;
import com.badgersmc.tsspear.domain.port.ReceiptStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryReceiptStore implements ReceiptStore {
    private final Map<UUID, List<ConfidenceReceipt>> byPlayer = new ConcurrentHashMap<>();

    @Override
    public void store(ConfidenceReceipt receipt) {
        byPlayer.computeIfAbsent(receipt.playerId(), k -> new ArrayList<>()).add(receipt);
    }

    @Override
    public List<ConfidenceReceipt> findByPlayer(UUID playerId, int limit) {
        List<ConfidenceReceipt> receipts = byPlayer.getOrDefault(playerId, List.of());
        int from = Math.max(0, receipts.size() - limit);
        return List.copyOf(receipts.subList(from, receipts.size()));
    }
}
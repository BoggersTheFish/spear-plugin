package com.badgersmc.tsspear.domain.port;

import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;

import java.util.List;
import java.util.UUID;

public interface ReceiptStore {
    void store(ConfidenceReceipt receipt);

    List<ConfidenceReceipt> findByPlayer(UUID playerId, int limit);
}
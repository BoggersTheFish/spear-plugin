package com.badgersmc.tsspear.application.bus;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;

import java.time.Instant;
import java.util.UUID;

public record ConfidenceChangedEvent(
    UUID playerId,
    ConfidenceDimension dimension,
    ConfidenceReceipt receipt,
    Instant timestamp
) implements TSEvent {}
package com.badgersmc.tsspear.application.bus;

import com.badgersmc.tsspear.domain.model.evidence.Evidence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EvidenceProducedEvent(UUID playerId, List<Evidence> evidence, Instant timestamp) implements TSEvent {}
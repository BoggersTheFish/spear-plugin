package com.badgersmc.tsspear.domain.model.replay;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReplaySequence(
    UUID sequenceId,
    UUID playerId,
    String playerName,
    long startTick,
    long endTick,
    Instant startTime,
    ReplayTriggerReason triggerReason,
    List<ReplayFrame> frames
) {}
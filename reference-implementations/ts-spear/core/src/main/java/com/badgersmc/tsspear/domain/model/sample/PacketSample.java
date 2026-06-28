package com.badgersmc.tsspear.domain.model.sample;

import com.badgersmc.tsspear.domain.model.world.WorldPosition;

import java.time.Instant;
import java.util.UUID;

public record PacketSample(
    UUID playerId,
    String playerName,
    long tick,
    Instant timestamp,
    String packetType,
    WorldPosition reportedPosition,
    WorldPosition serverPosition,
    double positionDelta,
    int pingMs,
    long millisSinceLastPacket
) {}
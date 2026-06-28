package com.badgersmc.tsspear.domain.model.sample;

import com.badgersmc.tsspear.domain.model.world.Vector3;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;

import java.time.Instant;
import java.util.UUID;

public record MovementSample(
    UUID playerId,
    long tick,
    Instant timestamp,
    WorldPosition from,
    WorldPosition to,
    Vector3 velocity,
    boolean onGround,
    boolean inWater,
    boolean inLava,
    boolean gliding,
    boolean sneaking,
    float fallDistance,
    int pingMs,
    String gameMode
) {
    public WorldPosition position() {
        return to;
    }
}
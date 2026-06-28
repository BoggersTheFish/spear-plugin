package com.badgersmc.tsspear.domain.model.sample;

import com.badgersmc.tsspear.domain.model.world.WorldPosition;

import java.time.Instant;
import java.util.UUID;

public record CombatSample(
    UUID attackerId,
    String attackerName,
    UUID victimId,
    String victimName,
    long tick,
    Instant timestamp,
    WorldPosition attackerPos,
    WorldPosition victimPos,
    double distance,
    double damage,
    boolean critical,
    int pingMs,
    String weaponType
) {}
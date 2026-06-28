package com.badgersmc.tsspear.domain.model.replay;

import com.badgersmc.tsspear.domain.model.world.Vector3;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;

import java.time.Instant;
import java.util.Map;

public record ReplayFrame(
    long tick,
    Instant timestamp,
    ReplayFrameType type,
    WorldPosition position,
    Vector3 velocity,
    boolean onGround,
    int pingMs,
    Map<String, Object> payload
) {
    public ReplayFrame {
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
}
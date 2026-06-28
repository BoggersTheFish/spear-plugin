package com.badgersmc.tsspear.application.pool;

import com.badgersmc.tsspear.domain.model.sample.MovementSample;
import com.badgersmc.tsspear.domain.model.world.Vector3;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;

import java.time.Instant;
import java.util.UUID;

public final class MovementSampleScratch {
    private UUID playerId;
    private long tick;
    private Instant timestamp;
    private WorldPosition from;
    private WorldPosition to;
    private Vector3 velocity;
    private boolean onGround;
    private boolean inWater;
    private boolean inLava;
    private boolean gliding;
    private boolean sneaking;
    private float fallDistance;
    private int pingMs;
    private String gameMode = "SURVIVAL";

    public void reset() {
        playerId = null;
        tick = 0L;
        timestamp = Instant.EPOCH;
        from = null;
        to = null;
        velocity = null;
        onGround = false;
        inWater = false;
        inLava = false;
        gliding = false;
        sneaking = false;
        fallDistance = 0f;
        pingMs = 0;
        gameMode = "SURVIVAL";
    }

    public MovementSampleScratch fill(
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
        this.playerId = playerId;
        this.tick = tick;
        this.timestamp = timestamp;
        this.from = from;
        this.to = to;
        this.velocity = velocity;
        this.onGround = onGround;
        this.inWater = inWater;
        this.inLava = inLava;
        this.gliding = gliding;
        this.sneaking = sneaking;
        this.fallDistance = fallDistance;
        this.pingMs = pingMs;
        this.gameMode = gameMode;
        return this;
    }

    public MovementSample toImmutable() {
        return new MovementSample(
            playerId, tick, timestamp, from, to, velocity,
            onGround, inWater, inLava, gliding, sneaking,
            fallDistance, pingMs, gameMode
        );
    }
}
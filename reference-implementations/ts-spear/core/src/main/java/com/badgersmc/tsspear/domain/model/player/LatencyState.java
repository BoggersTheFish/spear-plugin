package com.badgersmc.tsspear.domain.model.player;

public final class LatencyState {
    private int currentPingMs;
    private int maxPingMs;
    private long lastSpikeTick;

    public int currentPingMs() {
        return currentPingMs;
    }

    public int maxPingMs() {
        return maxPingMs;
    }

    public long lastSpikeTick() {
        return lastSpikeTick;
    }

    public void update(int pingMs, long tick, int spikeThreshold) {
        this.currentPingMs = pingMs;
        this.maxPingMs = Math.max(maxPingMs, pingMs);
        if (pingMs >= spikeThreshold) {
            this.lastSpikeTick = tick;
        }
    }
}
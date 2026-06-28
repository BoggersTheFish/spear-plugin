package com.badgersmc.tsspear.domain.model.player;

import com.badgersmc.tsspear.domain.model.sample.PacketSample;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class NetworkingState {
    private static final int MAX_HISTORY = 32;

    private PacketSample lastSample;
    private Instant lastPacketTime = Instant.EPOCH;
    private final Deque<PacketSample> recentPackets = new ArrayDeque<>();

    public PacketSample lastSample() {
        return lastSample;
    }

    public Instant lastPacketTime() {
        return lastPacketTime;
    }

    public List<PacketSample> recentPackets() {
        return List.copyOf(recentPackets);
    }

    public void recordPacket(PacketSample sample) {
        this.lastSample = sample;
        this.lastPacketTime = sample.timestamp();
        recentPackets.addLast(sample);
        while (recentPackets.size() > MAX_HISTORY) {
            recentPackets.removeFirst();
        }
    }
}
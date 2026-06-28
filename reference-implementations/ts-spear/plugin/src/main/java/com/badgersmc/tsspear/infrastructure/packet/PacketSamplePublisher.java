package com.badgersmc.tsspear.infrastructure.packet;

import com.badgersmc.tsspear.application.TSSpearRuntime;
import com.badgersmc.tsspear.application.bus.PacketSampleEvent;
import com.badgersmc.tsspear.domain.model.sample.PacketSample;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketSamplePublisher {
    private final TSSpearRuntime runtime;
    private final Map<UUID, Instant> lastPacketTime = new ConcurrentHashMap<>();

    public PacketSamplePublisher(TSSpearRuntime runtime) {
        this.runtime = runtime;
    }

    public void publish(Player player, String packetType, WorldPosition reported, WorldPosition server, double delta) {
        Instant now = Instant.now();
        Instant previous = lastPacketTime.put(player.getUniqueId(), now);
        long intervalMs = previous == null ? 50 : Duration.between(previous, now).toMillis();

        PacketSample sample = new PacketSample(
            player.getUniqueId(),
            player.getName(),
            player.getWorld().getFullTime(),
            now,
            packetType,
            reported,
            server,
            delta,
            player.getPing(),
            intervalMs
        );
        runtime.stateEngine().getOrCreate(player.getUniqueId(), player.getName());
        runtime.eventBus().publish(new PacketSampleEvent(sample, now));
    }
}
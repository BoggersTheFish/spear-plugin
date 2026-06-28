package com.badgersmc.tsspear.infrastructure.listener;

import com.badgersmc.tsspear.application.TSSpearRuntime;
import com.badgersmc.tsspear.application.bus.PlayerSampleEvent;
import com.badgersmc.tsspear.application.pool.MovementSampleScratch;
import com.badgersmc.tsspear.application.pool.PoolRegistry;
import com.badgersmc.tsspear.domain.model.sample.MovementSample;
import com.badgersmc.tsspear.domain.model.world.Vector3;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;
import com.badgersmc.tsspear.infrastructure.packet.PacketSamplePublisher;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MovementListener implements Listener {
    private final TSSpearRuntime runtime;
    private final PacketSamplePublisher packetPublisher;
    private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();

    public MovementListener(TSSpearRuntime runtime, PacketSamplePublisher packetPublisher) {
        this.runtime = runtime;
        this.packetPublisher = packetPublisher;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getX() == event.getTo().getX()
            && event.getFrom().getY() == event.getTo().getY()
            && event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }

        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        Location previous = lastLocations.put(player.getUniqueId(), to.clone());
        if (previous == null) {
            previous = from;
        }

        WorldPosition fromPos = toPosition(from);
        WorldPosition toPos = toPosition(to);
        Vector3 velocity = Vector3.between(fromPos, toPos);
        Instant now = Instant.now();

        MovementSample sample = buildSample(
            player, fromPos, toPos, velocity, now
        );

        WorldPosition serverPos = toPos;
        packetPublisher.publish(player, "MOVEMENT", toPos, serverPos, 0.0);

        runtime.stateEngine().getOrCreate(player.getUniqueId(), player.getName());
        runtime.eventBus().publish(new PlayerSampleEvent(sample, now));
    }

    private MovementSample buildSample(
        Player player,
        WorldPosition fromPos,
        WorldPosition toPos,
        Vector3 velocity,
        Instant now
    ) {
        PoolRegistry pools = PoolRegistry.get();
        if (pools != null && pools.enabled()) {
            MovementSampleScratch scratch = pools.acquireMovementScratch();
            try {
                return scratch.fill(
                    player.getUniqueId(),
                    player.getWorld().getFullTime(),
                    now,
                    fromPos,
                    toPos,
                    velocity,
                    player.isOnGround(),
                    player.isInWater(),
                    player.isInLava(),
                    player.isGliding(),
                    player.isSneaking(),
                    player.getFallDistance(),
                    player.getPing(),
                    gameModeName(player.getGameMode())
                ).toImmutable();
            } finally {
                pools.releaseMovementScratch(scratch);
            }
        }
        return new MovementSample(
            player.getUniqueId(),
            player.getWorld().getFullTime(),
            now,
            fromPos,
            toPos,
            velocity,
            player.isOnGround(),
            player.isInWater(),
            player.isInLava(),
            player.isGliding(),
            player.isSneaking(),
            player.getFallDistance(),
            player.getPing(),
            gameModeName(player.getGameMode())
        );
    }

    private static WorldPosition toPosition(Location loc) {
        return new WorldPosition(
            loc.getWorld().getName(),
            loc.getX(),
            loc.getY(),
            loc.getZ(),
            loc.getYaw(),
            loc.getPitch()
        );
    }

    private static String gameModeName(GameMode mode) {
        return mode == null ? "UNKNOWN" : mode.name();
    }
}
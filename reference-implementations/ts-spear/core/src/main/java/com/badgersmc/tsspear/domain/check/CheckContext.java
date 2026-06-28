package com.badgersmc.tsspear.domain.check;

import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.model.sample.CombatSample;
import com.badgersmc.tsspear.domain.model.sample.MovementSample;
import com.badgersmc.tsspear.domain.model.sample.PacketSample;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;

import java.time.Instant;
import java.util.Optional;

public record CheckContext(
    PlayerState state,
    MovementSample movementSample,
    CombatSample combatSample,
    PacketSample packetSample,
    CheckConfig config,
    Instant now
) {
    public static CheckContext forMovement(PlayerState state, MovementSample sample, CheckConfig config, Instant now) {
        return new CheckContext(state, sample, null, null, config, now);
    }

    public static CheckContext forCombat(PlayerState state, CombatSample sample, CheckConfig config, Instant now) {
        return new CheckContext(state, null, sample, null, config, now);
    }

    public static CheckContext forPacket(PlayerState state, PacketSample sample, CheckConfig config, Instant now) {
        return new CheckContext(state, null, null, sample, config, now);
    }

    /** @deprecated use {@link #movementSample()} */
    @Deprecated
    public MovementSample sample() {
        return movementSample;
    }

    public Optional<MovementSample> movementSampleOptional() {
        return Optional.ofNullable(movementSample);
    }

    public Optional<CombatSample> combatSampleOptional() {
        return Optional.ofNullable(combatSample);
    }

    public WorldPosition playerLocation() {
        if (movementSample != null) {
            return movementSample.position();
        }
        if (combatSample != null) {
            return combatSample.attackerPos();
        }
        if (packetSample != null) {
            return packetSample.serverPosition();
        }
        return null;
    }
}
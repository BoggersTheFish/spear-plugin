package com.badgersmc.tsspear.check.movement;

import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.check.AbstractMovementCheck;
import com.badgersmc.tsspear.domain.check.CheckContext;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.CheckId;
import com.badgersmc.tsspear.domain.model.sample.MovementSample;

import java.util.List;
import java.util.Map;

public final class FlyCheck extends AbstractMovementCheck {
    public static final CheckId ID = CheckId.of("fly-check");

    public FlyCheck() {
        super(ID);
    }

    @Override
    public List<Evidence> evaluate(CheckContext ctx) {
        MovementSample current = ctx.sample();
        if (current == null) {
            return List.of();
        }
        if ("CREATIVE".equals(current.gameMode()) || "SPECTATOR".equals(current.gameMode())) {
            return List.of();
        }
        if (current.gliding() || current.inWater() || current.inLava()) {
            return List.of();
        }
        MovementSample previous = previousSample(ctx);
        if (previous == null) {
            return List.of();
        }

        int airTicks = countAirTicks(ctx, current);
        int maxAir = ctx.config().getInt("max-air-ticks", 40);
        if (airTicks <= maxAir) {
            return List.of();
        }

        double severity = clamp((airTicks - maxAir) / (double) maxAir);
        return List.of(buildEvidence(
            EvidenceKind.FLY_VIOLATION,
            severity,
            0.85,
            ctx,
            "Air time %d ticks exceeds limit %d while not gliding/in water".formatted(airTicks, maxAir),
            Map.of("airTicks", airTicks, "limit", maxAir, "tick", current.tick())
        ));
    }

    private int countAirTicks(CheckContext ctx, MovementSample current) {
        MovementSample prev = previousSample(ctx);
        if (prev == null || current.onGround()) {
            return current.onGround() ? 0 : 1;
        }
        int streak = prev.onGround() ? 1 : 2;
        return current.onGround() ? 0 : streak;
    }
}
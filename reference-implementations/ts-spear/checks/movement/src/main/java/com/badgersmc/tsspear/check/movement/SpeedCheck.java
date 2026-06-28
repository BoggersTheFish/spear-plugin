package com.badgersmc.tsspear.check.movement;

import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.check.AbstractMovementCheck;
import com.badgersmc.tsspear.domain.check.CheckContext;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.CheckId;
import com.badgersmc.tsspear.domain.model.sample.MovementSample;

import java.util.List;
import java.util.Map;

public final class SpeedCheck extends AbstractMovementCheck {
    public static final CheckId ID = CheckId.of("speed-check");

    public SpeedCheck() {
        super(ID);
    }

    @Override
    public List<Evidence> evaluate(CheckContext ctx) {
        MovementSample current = ctx.sample();
        if (current == null) {
            return List.of();
        }
        double speed = horizontalSpeed(current);
        double maxSpeed = ctx.config().getDouble("max-speed", 0.65);
        if (speed <= maxSpeed) {
            return List.of();
        }
        double severity = clamp((speed - maxSpeed) / maxSpeed);
        return List.of(buildEvidence(
            EvidenceKind.SPEED_VIOLATION,
            severity,
            0.90,
            ctx,
            "Horizontal speed %.3f exceeds limit %.3f blocks/tick".formatted(speed, maxSpeed),
            Map.of("observed", speed, "limit", maxSpeed, "tick", current.tick())
        ));
    }

    private double horizontalSpeed(MovementSample sample) {
        return Math.sqrt(sample.velocity().x() * sample.velocity().x()
            + sample.velocity().z() * sample.velocity().z());
    }
}
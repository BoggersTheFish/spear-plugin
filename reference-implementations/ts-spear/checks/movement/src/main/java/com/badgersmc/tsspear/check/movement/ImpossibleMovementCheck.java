package com.badgersmc.tsspear.check.movement;

import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.check.AbstractMovementCheck;
import com.badgersmc.tsspear.domain.check.CheckContext;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.CheckId;
import com.badgersmc.tsspear.domain.model.sample.MovementSample;

import java.util.List;
import java.util.Map;

public final class ImpossibleMovementCheck extends AbstractMovementCheck {
    public static final CheckId ID = CheckId.of("impossible-movement");

    public ImpossibleMovementCheck() {
        super(ID);
    }

    @Override
    public List<Evidence> evaluate(CheckContext ctx) {
        MovementSample current = ctx.sample();
        if (current == null) {
            return List.of();
        }
        MovementSample previous = previousSample(ctx);
        if (previous == null) {
            return List.of();
        }

        double maxAccel = ctx.config().getDouble("max-acceleration", 0.52);
        double observed = acceleration(previous, current);
        if (observed <= maxAccel) {
            return List.of();
        }

        double severity = normalize(observed, maxAccel);
        return List.of(buildEvidence(
            EvidenceKind.IMPOSSIBLE_ACCELERATION,
            severity,
            0.95,
            ctx,
            "Acceleration %.3f exceeds limit %.3f blocks/tick²".formatted(observed, maxAccel),
            Map.of(
                "observed", observed,
                "limit", maxAccel,
                "tick", current.tick(),
                "fromX", previous.position().x(),
                "fromY", previous.position().y(),
                "fromZ", previous.position().z(),
                "toX", current.position().x(),
                "toY", current.position().y(),
                "toZ", current.position().z()
            )
        ));
    }

    private double normalize(double observed, double limit) {
        return clamp((observed - limit) / (limit * 2.0));
    }
}
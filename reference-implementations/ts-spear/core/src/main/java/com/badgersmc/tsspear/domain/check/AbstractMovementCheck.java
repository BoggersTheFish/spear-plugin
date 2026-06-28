package com.badgersmc.tsspear.domain.check;

import com.badgersmc.tsspear.api.check.CheckCategory;
import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.domain.model.ids.CheckId;
import com.badgersmc.tsspear.domain.model.sample.MovementSample;

public abstract class AbstractMovementCheck extends AbstractCheck {
    protected AbstractMovementCheck(CheckId id) {
        super(id, CheckCategory.MOVEMENT, ConfidenceDimension.MOVEMENT);
    }

    protected MovementSample previousSample(CheckContext ctx) {
        return ctx.state().movement().lastSample();
    }

    protected double acceleration(MovementSample prev, MovementSample curr) {
        if (prev == null) {
            return 0.0;
        }
        long dt = Math.max(1, curr.tick() - prev.tick());
        double dv = curr.velocity().length() - prev.velocity().length();
        return Math.abs(dv / dt);
    }
}
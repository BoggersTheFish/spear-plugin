package com.badgersmc.tsspear.check.combat;

import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.check.AbstractCombatCheck;
import com.badgersmc.tsspear.domain.check.CheckContext;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.CheckId;
import com.badgersmc.tsspear.domain.model.sample.CombatSample;

import java.util.List;
import java.util.Map;

public final class VelocityCheck extends AbstractCombatCheck {
    public static final CheckId ID = CheckId.of("velocity-check");

    public VelocityCheck() {
        super(ID);
    }

    @Override
    public List<Evidence> evaluate(CheckContext ctx) {
        CombatSample sample = combatSample(ctx);
        if (sample == null) {
            return List.of();
        }
        double maxKnockback = ctx.config().getDouble("max-knockback-damage-ratio", 0.5);
        if (sample.damage() <= 0) {
            return List.of();
        }
        double ratio = sample.damage() / Math.max(1.0, sample.distance());
        if (ratio <= maxKnockback * 10) {
            return List.of();
        }
        double severity = clamp(ratio / 20.0);
        return List.of(buildEvidence(
            EvidenceKind.VELOCITY_ANOMALY,
            severity,
            0.75,
            ctx,
            "Knockback ratio %.2f suggests velocity modification".formatted(ratio),
            Map.of("ratio", ratio, "damage", sample.damage())
        ));
    }
}
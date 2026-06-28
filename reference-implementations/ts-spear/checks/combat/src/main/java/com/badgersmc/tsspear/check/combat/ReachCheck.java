package com.badgersmc.tsspear.check.combat;

import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.check.AbstractCombatCheck;
import com.badgersmc.tsspear.domain.check.CheckContext;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.CheckId;
import com.badgersmc.tsspear.domain.model.sample.CombatSample;

import java.util.List;
import java.util.Map;

public final class ReachCheck extends AbstractCombatCheck {
    public static final CheckId ID = CheckId.of("reach-check");

    public ReachCheck() {
        super(ID);
    }

    @Override
    public List<Evidence> evaluate(CheckContext ctx) {
        CombatSample sample = combatSample(ctx);
        if (sample == null) {
            return List.of();
        }
        double maxReach = ctx.config().getDouble("max-reach", 3.2);
        double distance = sample.distance();
        if (distance <= maxReach) {
            return List.of();
        }
        double severity = clamp((distance - maxReach) / maxReach);
        return List.of(buildEvidence(
            EvidenceKind.REACH_EXCESS,
            severity,
            0.92,
            ctx,
            "Hit distance %.2f exceeds reach limit %.2f".formatted(distance, maxReach),
            Map.of("distance", distance, "limit", maxReach, "victim", sample.victimName())
        ));
    }
}
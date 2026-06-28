package com.badgersmc.tsspear.check.combat;

import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.check.AbstractCombatCheck;
import com.badgersmc.tsspear.domain.check.CheckContext;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.CheckId;
import com.badgersmc.tsspear.domain.model.sample.CombatSample;

import java.util.List;
import java.util.Map;

public final class KillAuraCheck extends AbstractCombatCheck {
    public static final CheckId ID = CheckId.of("killaura-check");

    public KillAuraCheck() {
        super(ID);
    }

    @Override
    public List<Evidence> evaluate(CheckContext ctx) {
        CombatSample current = combatSample(ctx);
        if (current == null) {
            return List.of();
        }
        List<CombatSample> recent = ctx.state().combat().recentHits();
        if (recent.size() < 3) {
            return List.of();
        }

        int windowTicks = ctx.config().getInt("killaura-window-ticks", 10);
        int distinctVictims = 0;
        long oldest = current.tick() - windowTicks;
        java.util.Set<java.util.UUID> victims = new java.util.HashSet<>();
        for (int i = recent.size() - 1; i >= 0; i--) {
            CombatSample hit = recent.get(i);
            if (hit.tick() < oldest) {
                break;
            }
            victims.add(hit.victimId());
        }
        distinctVictims = victims.size();
        int threshold = ctx.config().getInt("killaura-victim-threshold", 3);
        if (distinctVictims < threshold) {
            return List.of();
        }

        double severity = clamp(distinctVictims / (double) (threshold + 1));
        return List.of(buildEvidence(
            EvidenceKind.KILL_AURA_PATTERN,
            severity,
            0.80,
            ctx,
            "%d distinct victims hit within %d ticks".formatted(distinctVictims, windowTicks),
            Map.of("victims", distinctVictims, "windowTicks", windowTicks)
        ));
    }
}
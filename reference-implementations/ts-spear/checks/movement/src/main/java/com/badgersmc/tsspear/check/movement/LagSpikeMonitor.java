package com.badgersmc.tsspear.check.movement;

import com.badgersmc.tsspear.api.check.CheckCategory;
import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.check.AbstractCheck;
import com.badgersmc.tsspear.domain.check.CheckContext;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.CheckId;
import com.badgersmc.tsspear.domain.model.sample.MovementSample;

import java.util.List;
import java.util.Map;

public final class LagSpikeMonitor extends AbstractCheck {
    public static final CheckId ID = CheckId.of("lag-spike");

    public LagSpikeMonitor() {
        super(ID, CheckCategory.NETWORKING, ConfidenceDimension.MOVEMENT);
    }

    @Override
    public List<Evidence> evaluate(CheckContext ctx) {
        MovementSample sample = ctx.sample();
        if (sample == null) {
            return List.of();
        }
        int ping = sample.pingMs();
        int threshold = ctx.config().getInt("lag-threshold-ms", 200);
        ctx.state().latency().update(ping, sample.tick(), threshold);
        if (ping < threshold) {
            return List.of();
        }
        double severity = clamp((ping - threshold) / (double) threshold);
        return List.of(buildEvidence(
            EvidenceKind.LAG_SPIKE,
            severity,
            0.80,
            ctx,
            "Ping %dms exceeds threshold %dms — may explain movement anomalies".formatted(ping, threshold),
            Map.of("ping", ping, "threshold", threshold)
        ));
    }
}
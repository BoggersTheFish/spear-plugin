package com.badgersmc.tsspear.domain.check;

import com.badgersmc.tsspear.api.check.CheckCategory;
import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.CheckId;

import java.util.Map;

public abstract class AbstractCheck implements Check {
    private final CheckId id;
    private final CheckCategory category;
    private final ConfidenceDimension dimension;

    protected AbstractCheck(CheckId id, CheckCategory category, ConfidenceDimension dimension) {
        this.id = id;
        this.category = category;
        this.dimension = dimension;
    }

    @Override
    public final CheckId id() {
        return id;
    }

    @Override
    public final CheckCategory category() {
        return category;
    }

    @Override
    public final ConfidenceDimension dimension() {
        return dimension;
    }

    @Override
    public boolean isEnabled(CheckConfig config) {
        return config.isEnabled(id.value());
    }

    protected Evidence buildEvidence(
        EvidenceKind kind,
        double severity,
        double credibility,
        CheckContext ctx,
        String reason,
        Map<String, Object> payload
    ) {
        var pools = com.badgersmc.tsspear.application.pool.PoolRegistry.get();
        Evidence.Builder builder = pools != null && pools.enabled()
            ? pools.evidenceBuilders().acquire()
            : Evidence.builder();
        try {
            return builder
                .playerId(ctx.state().uuid())
                .kind(kind)
                .dimension(dimension)
                .severity(severity)
                .credibility(credibility)
                .timestamp(ctx.now())
                .tick(sampleTick(ctx))
                .location(ctx.playerLocation())
                .reason(reason)
                .sourceCheckId(id.value())
                .payload(payload)
                .build();
        } finally {
            if (pools != null && pools.enabled()) {
                pools.evidenceBuilders().release(builder);
            }
        }
    }

    protected static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static long sampleTick(CheckContext ctx) {
        if (ctx.movementSample() != null) {
            return ctx.movementSample().tick();
        }
        if (ctx.combatSample() != null) {
            return ctx.combatSample().tick();
        }
        if (ctx.packetSample() != null) {
            return ctx.packetSample().tick();
        }
        return 0L;
    }
}
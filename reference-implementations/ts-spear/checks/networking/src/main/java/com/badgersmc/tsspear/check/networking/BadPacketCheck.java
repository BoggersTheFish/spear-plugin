package com.badgersmc.tsspear.check.networking;

import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.check.AbstractNetworkingCheck;
import com.badgersmc.tsspear.domain.check.CheckContext;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.CheckId;
import com.badgersmc.tsspear.domain.model.sample.PacketSample;

import java.util.List;
import java.util.Map;

public final class BadPacketCheck extends AbstractNetworkingCheck {
    public static final CheckId ID = CheckId.of("bad-packet-check");

    public BadPacketCheck() {
        super(ID);
    }

    @Override
    public List<Evidence> evaluate(CheckContext ctx) {
        PacketSample sample = packetSample(ctx);
        if (sample == null) {
            return List.of();
        }
        double threshold = ctx.config().getDouble("bad-packet-position-threshold", 0.5);
        if (sample.positionDelta() <= threshold) {
            return List.of();
        }
        double severity = clamp(sample.positionDelta() / (threshold * 4));
        return List.of(buildEvidence(
            EvidenceKind.BAD_PACKET,
            severity,
            0.85,
            ctx,
            "Reported position delta %.2f exceeds threshold %.2f".formatted(sample.positionDelta(), threshold),
            Map.of("delta", sample.positionDelta(), "packet", sample.packetType())
        ));
    }
}
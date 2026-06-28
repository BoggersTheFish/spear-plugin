package com.badgersmc.tsspear.check.networking;

import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.check.AbstractNetworkingCheck;
import com.badgersmc.tsspear.domain.check.CheckContext;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.CheckId;
import com.badgersmc.tsspear.domain.model.sample.PacketSample;

import java.util.List;
import java.util.Map;

public final class BlinkCheck extends AbstractNetworkingCheck {
    public static final CheckId ID = CheckId.of("blink-check");

    public BlinkCheck() {
        super(ID);
    }

    @Override
    public List<Evidence> evaluate(CheckContext ctx) {
        PacketSample sample = packetSample(ctx);
        if (sample == null) {
            return List.of();
        }
        int minInterval = ctx.config().getInt("blink-min-interval-ms", 40);
        if (sample.millisSinceLastPacket() >= minInterval) {
            return List.of();
        }
        double severity = clamp(1.0 - (sample.millisSinceLastPacket() / (double) minInterval));
        return List.of(buildEvidence(
            EvidenceKind.BLINK,
            severity,
            0.78,
            ctx,
            "Position packet interval %dms below minimum %dms".formatted(sample.millisSinceLastPacket(), minInterval),
            Map.of("intervalMs", sample.millisSinceLastPacket(), "pingMs", sample.pingMs())
        ));
    }
}
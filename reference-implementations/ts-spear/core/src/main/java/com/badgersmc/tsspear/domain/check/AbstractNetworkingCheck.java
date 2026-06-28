package com.badgersmc.tsspear.domain.check;

import com.badgersmc.tsspear.api.check.CheckCategory;
import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.domain.model.ids.CheckId;
import com.badgersmc.tsspear.domain.model.sample.PacketSample;

public abstract class AbstractNetworkingCheck extends AbstractCheck {
    protected AbstractNetworkingCheck(CheckId id) {
        super(id, CheckCategory.NETWORKING, ConfidenceDimension.NETWORKING);
    }

    protected PacketSample packetSample(CheckContext ctx) {
        return ctx.packetSample();
    }
}
package com.badgersmc.tsspear.application.config;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.application.scheduler.DecayScheduler;
import com.badgersmc.tsspear.domain.model.confidence.DecayCurve;

import java.util.EnumMap;
import java.util.Map;

public final class DecaySettings {
    private final Map<ConfidenceDimension, DecayScheduler.DecayCurveConfig> perDimension;

    public DecaySettings(Map<ConfidenceDimension, DecayScheduler.DecayCurveConfig> perDimension) {
        this.perDimension = Map.copyOf(perDimension);
    }

    public Map<ConfidenceDimension, DecayScheduler.DecayCurveConfig> perDimension() {
        return perDimension;
    }

    public static DecaySettings defaults() {
        Map<ConfidenceDimension, DecayScheduler.DecayCurveConfig> map = new EnumMap<>(ConfidenceDimension.class);
        map.put(ConfidenceDimension.MOVEMENT, new DecayScheduler.DecayCurveConfig(DecayCurve.EXPONENTIAL, 0.02));
        map.put(ConfidenceDimension.COMBAT, new DecayScheduler.DecayCurveConfig(DecayCurve.LINEAR, 0.01));
        map.put(ConfidenceDimension.INVENTORY, new DecayScheduler.DecayCurveConfig(DecayCurve.LINEAR, 0.01));
        map.put(ConfidenceDimension.AUTOMATION, new DecayScheduler.DecayCurveConfig(DecayCurve.EXPONENTIAL, 0.015));
        map.put(ConfidenceDimension.NETWORKING, new DecayScheduler.DecayCurveConfig(DecayCurve.LINEAR, 0.02));
        map.put(ConfidenceDimension.INTERACTION, new DecayScheduler.DecayCurveConfig(DecayCurve.LINEAR, 0.01));
        return new DecaySettings(map);
    }
}
package com.badgersmc.tsspear.domain.service;

import com.badgersmc.tsspear.domain.model.confidence.DecayCurve;

import java.time.Duration;

public final class DecayApplicator {
    public double apply(double confidence, DecayCurve curve, Duration elapsed, double decayRate) {
        double seconds = Math.max(0, elapsed.toMillis() / 1000.0);
        return switch (curve) {
            case LINEAR -> Math.max(0, confidence - decayRate * seconds);
            case EXPONENTIAL -> confidence * Math.exp(-decayRate * seconds);
            case LOGARITHMIC -> Math.max(0, confidence - decayRate * Math.log1p(seconds));
        };
    }
}
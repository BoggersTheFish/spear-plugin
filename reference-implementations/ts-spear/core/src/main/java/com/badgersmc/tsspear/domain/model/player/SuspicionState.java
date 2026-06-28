package com.badgersmc.tsspear.domain.model.player;

import com.badgersmc.tsspear.api.confidence.TrendDirection;

import java.time.Instant;

public final class SuspicionState {
    private double currentRisk;
    private double previousRisk;
    private TrendDirection trend = TrendDirection.STABLE;
    private Instant updatedAt = Instant.now();

    public void update(double risk) {
        if (risk > previousRisk + 0.01) {
            trend = TrendDirection.RISING;
        } else if (risk < previousRisk - 0.01) {
            trend = TrendDirection.FALLING;
        } else {
            trend = TrendDirection.STABLE;
        }
        this.previousRisk = currentRisk;
        this.currentRisk = risk;
        this.updatedAt = Instant.now();
    }

    public double currentRisk() {
        return currentRisk;
    }

    public double previousRisk() {
        return previousRisk;
    }

    public double delta() {
        return currentRisk - previousRisk;
    }

    public TrendDirection trend() {
        return trend;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
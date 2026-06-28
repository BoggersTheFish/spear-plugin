package com.badgersmc.tsspear.domain.model.player;

import com.badgersmc.tsspear.api.confidence.TrendDirection;
import com.badgersmc.tsspear.domain.model.confidence.ConfidenceVector;

import java.time.Instant;

public final class TrustState {
    private ConfidenceVector vector = ConfidenceVector.empty();
    private double previousAggregate;
    private TrendDirection trend = TrendDirection.STABLE;
    private Instant evaluatedAt = Instant.now();

    public ConfidenceVector vector() {
        return vector;
    }

    public void setVector(ConfidenceVector vector) {
        double aggregate = vector.aggregate();
        if (aggregate > previousAggregate + 0.01) {
            trend = TrendDirection.RISING;
        } else if (aggregate < previousAggregate - 0.01) {
            trend = TrendDirection.FALLING;
        } else {
            trend = TrendDirection.STABLE;
        }
        this.previousAggregate = aggregate;
        this.vector = vector;
        this.evaluatedAt = Instant.now();
    }

    public double aggregateRisk() {
        return vector.aggregate();
    }

    public double netConfidence() {
        return 1.0 - vector.aggregate();
    }

    public TrendDirection trend() {
        return trend;
    }

    public double trendDelta() {
        return vector.aggregate() - previousAggregate;
    }

    public Instant evaluatedAt() {
        return evaluatedAt;
    }
}
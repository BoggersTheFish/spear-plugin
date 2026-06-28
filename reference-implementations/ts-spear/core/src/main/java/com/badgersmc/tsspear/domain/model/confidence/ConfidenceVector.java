package com.badgersmc.tsspear.domain.model.confidence;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;

import java.util.EnumMap;
import java.util.Map;

public final class ConfidenceVector {
    private final Map<ConfidenceDimension, DimensionConfidence> dimensions;

    public ConfidenceVector(Map<ConfidenceDimension, DimensionConfidence> dimensions) {
        this.dimensions = new EnumMap<>(dimensions);
    }

    public static ConfidenceVector empty() {
        return new ConfidenceVector(DimensionConfidence.emptyVector());
    }

    public DimensionConfidence get(ConfidenceDimension dimension) {
        return dimensions.computeIfAbsent(dimension, DimensionConfidence::new);
    }

    public Map<ConfidenceDimension, DimensionConfidence> asMap() {
        return Map.copyOf(dimensions);
    }

    public double aggregate() {
        double sum = 0;
        int count = 0;
        for (Map.Entry<ConfidenceDimension, DimensionConfidence> entry : dimensions.entrySet()) {
            if (entry.getKey() == ConfidenceDimension.AGGREGATE) {
                continue;
            }
            sum += entry.getValue().confidence();
            count++;
        }
        return count == 0 ? 0.0 : sum / count;
    }

    public ConfidenceVector snapshot() {
        Map<ConfidenceDimension, DimensionConfidence> copy = new EnumMap<>(ConfidenceDimension.class);
        for (Map.Entry<ConfidenceDimension, DimensionConfidence> entry : dimensions.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().snapshot());
        }
        return new ConfidenceVector(copy);
    }
}
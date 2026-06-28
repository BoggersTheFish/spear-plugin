package com.badgersmc.tsspear.domain.service;

public final class ConfidenceResolver {
    private final double tensionSensitivity;

    public ConfidenceResolver(double tensionSensitivity) {
        this.tensionSensitivity = tensionSensitivity;
    }

    public double resolve(double activation, double tension) {
        double adjusted = activation - tension * tensionSensitivity;
        return sigmoid(adjusted);
    }

    public double netConfidence(double confidence, double supportWeight, double conflictWeight) {
        return sigmoid(confidence - conflictWeight * 0.5 + supportWeight * 0.1);
    }

    private static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
}
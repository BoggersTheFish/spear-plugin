package com.badgersmc.tsspear.application.ml;

public record PredictionResult(
    String providerId,
    boolean abstain,
    double riskScore,
    String explanation
) {
    public static PredictionResult abstain(String providerId, String explanation) {
        return new PredictionResult(providerId, true, 0.0, explanation);
    }
}
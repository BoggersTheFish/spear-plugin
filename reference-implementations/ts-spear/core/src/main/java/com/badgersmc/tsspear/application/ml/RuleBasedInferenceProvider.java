package com.badgersmc.tsspear.application.ml;

import com.badgersmc.tsspear.domain.port.InferenceProvider;

import java.util.Map;

public final class RuleBasedInferenceProvider implements InferenceProvider {
    private final double cheatingThreshold;
    private final double suspiciousThreshold;

    public RuleBasedInferenceProvider(double cheatingThreshold, double suspiciousThreshold) {
        this.cheatingThreshold = cheatingThreshold;
        this.suspiciousThreshold = suspiciousThreshold;
    }

    @Override
    public String providerId() {
        return "rule-based";
    }

    @Override
    public InferenceResult infer(InferenceRequest request) {
        float[] features = request.features();
        if (features == null || features.length == 0) {
            return InferenceResult.abstain(providerId(), "No features supplied");
        }

        double suspicion = value(features, "suspicion_risk");
        double aggregate = value(features, "aggregate_confidence");
        double tension = value(features, "tension_aggregate");
        double networking = value(features, "networking_confidence");
        double packetMaxDelta = value(features, "packet_max_delta");

        if (suspicion >= cheatingThreshold
            || aggregate >= cheatingThreshold
            || (networking >= suspiciousThreshold && packetMaxDelta > 1.0)) {
            double confidence = Math.min(1.0, Math.max(suspicion, aggregate));
            return InferenceResult.prediction(
                providerId(),
                confidence,
                "CHEATING",
                String.format(
                    "suspicion=%.2f aggregate=%.2f networking=%.2f packetMax=%.2f",
                    suspicion, aggregate, networking, packetMaxDelta
                )
            ).withMetadata(Map.of(
                "suspicion", suspicion,
                "aggregate", aggregate,
                "tension", tension
            ));
        }

        if (suspicion >= suspiciousThreshold || tension >= 0.35 || networking >= suspiciousThreshold) {
            double confidence = Math.max(suspicion, tension * 0.5);
            return InferenceResult.prediction(
                providerId(),
                confidence,
                "SUSPICIOUS",
                String.format("suspicion=%.2f tension=%.2f networking=%.2f", suspicion, tension, networking)
            );
        }

        return InferenceResult.prediction(
            providerId(),
            1.0 - suspicion,
            "LEGIT",
            String.format("suspicion=%.2f below thresholds", suspicion)
        );
    }

    private double value(float[] features, String name) {
        int index = FeatureExtractor.indexOf(name);
        if (index < 0 || index >= features.length) {
            return 0.0;
        }
        return features[index];
    }
}
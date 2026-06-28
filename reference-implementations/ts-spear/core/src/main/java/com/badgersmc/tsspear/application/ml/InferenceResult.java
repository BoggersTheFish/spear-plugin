package com.badgersmc.tsspear.application.ml;

import java.util.Map;

public record InferenceResult(
    String providerId,
    boolean abstain,
    double confidence,
    String label,
    String reason,
    Map<String, Object> metadata
) {
    public static InferenceResult abstain(String providerId, String reason) {
        return new InferenceResult(providerId, true, 0.0, "ABSTAIN", reason, Map.of());
    }

    public static InferenceResult prediction(String providerId, double confidence, String label, String reason) {
        return new InferenceResult(providerId, false, confidence, label, reason, Map.of());
    }

    public InferenceResult withMetadata(Map<String, Object> extra) {
        java.util.HashMap<String, Object> merged = new java.util.HashMap<>(metadata);
        merged.putAll(extra);
        return new InferenceResult(providerId, abstain, confidence, label, reason, Map.copyOf(merged));
    }
}
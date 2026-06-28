package com.badgersmc.tsspear.application.ml;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RuleBasedInferenceProviderTest {
    @Test
    void flagsCheatingWhenSuspicionHigh() {
        RuleBasedInferenceProvider provider = new RuleBasedInferenceProvider(0.65, 0.40);
        float[] features = new float[FeatureExtractor.FEATURE_NAMES.length];
        features[FeatureExtractor.indexOf("suspicion_risk")] = 0.8f;

        InferenceResult result = provider.infer(new InferenceRequest(UUID.randomUUID(), features, null));

        assertFalse(result.abstain());
        assertEquals("CHEATING", result.label());
    }

    @Test
    void flagsSuspiciousWhenTensionElevated() {
        RuleBasedInferenceProvider provider = new RuleBasedInferenceProvider(0.65, 0.40);
        float[] features = new float[FeatureExtractor.FEATURE_NAMES.length];
        features[FeatureExtractor.indexOf("tension_aggregate")] = 0.5f;

        InferenceResult result = provider.infer(new InferenceRequest(UUID.randomUUID(), features, null));

        assertEquals("SUSPICIOUS", result.label());
    }

    @Test
    void defaultsToLegitWhenSignalsLow() {
        RuleBasedInferenceProvider provider = new RuleBasedInferenceProvider(0.65, 0.40);
        float[] features = new float[FeatureExtractor.FEATURE_NAMES.length];
        features[FeatureExtractor.indexOf("suspicion_risk")] = 0.1f;

        InferenceResult result = provider.infer(new InferenceRequest(UUID.randomUUID(), features, null));

        assertEquals("LEGIT", result.label());
    }
}
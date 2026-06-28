package com.badgersmc.tsspear.application.service;

import com.badgersmc.tsspear.application.ml.FeatureExtractor;
import com.badgersmc.tsspear.application.ml.InferenceRequest;
import com.badgersmc.tsspear.application.ml.InferenceResult;
import com.badgersmc.tsspear.application.ml.MlProviderRegistry;
import com.badgersmc.tsspear.domain.model.player.PlayerState;

public final class InferenceService {
    private final MlProviderRegistry registry;
    private final FeatureExtractor featureExtractor;

    public InferenceService(MlProviderRegistry registry, FeatureExtractor featureExtractor) {
        this.registry = registry;
        this.featureExtractor = featureExtractor;
    }

    public InferenceResult infer(PlayerState state, String modelHint) {
        return registry.active()
            .map(provider -> provider.infer(new InferenceRequest(
                state.uuid(),
                featureExtractor.extract(state).values(),
                modelHint
            )))
            .orElseGet(() -> InferenceResult.abstain("none", "No inference provider configured"));
    }

    public MlProviderRegistry registry() {
        return registry;
    }
}
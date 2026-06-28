package com.badgersmc.tsspear.application.ml;

import com.badgersmc.tsspear.domain.port.InferenceProvider;

public final class NoOpInferenceProvider implements InferenceProvider {
    @Override
    public String providerId() {
        return "noop";
    }

    @Override
    public InferenceResult infer(InferenceRequest request) {
        return InferenceResult.abstain(providerId(), "ML provider not configured");
    }
}
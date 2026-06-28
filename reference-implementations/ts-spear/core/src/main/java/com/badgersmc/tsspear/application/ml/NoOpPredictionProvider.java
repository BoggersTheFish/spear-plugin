package com.badgersmc.tsspear.application.ml;

import com.badgersmc.tsspear.domain.port.PredictionProvider;

public final class NoOpPredictionProvider implements PredictionProvider {
    @Override
    public String providerId() {
        return "noop";
    }

    @Override
    public PredictionResult predict(PredictionRequest request) {
        return PredictionResult.abstain(providerId(), "Prediction provider not configured");
    }
}
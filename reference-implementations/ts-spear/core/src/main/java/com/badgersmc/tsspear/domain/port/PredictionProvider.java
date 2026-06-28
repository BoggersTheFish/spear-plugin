package com.badgersmc.tsspear.domain.port;

import com.badgersmc.tsspear.application.ml.PredictionRequest;
import com.badgersmc.tsspear.application.ml.PredictionResult;

public interface PredictionProvider {
    String providerId();

    PredictionResult predict(PredictionRequest request);
}
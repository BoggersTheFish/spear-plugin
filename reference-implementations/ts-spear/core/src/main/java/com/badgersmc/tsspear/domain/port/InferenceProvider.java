package com.badgersmc.tsspear.domain.port;

import com.badgersmc.tsspear.application.ml.InferenceRequest;
import com.badgersmc.tsspear.application.ml.InferenceResult;

public interface InferenceProvider {
    String providerId();

    InferenceResult infer(InferenceRequest request);
}
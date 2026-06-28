package com.badgersmc.tsspear.application.ml;

import java.util.UUID;

public record InferenceRequest(
    UUID playerId,
    float[] features,
    String modelHint
) {}
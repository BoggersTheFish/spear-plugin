package com.badgersmc.tsspear.application.ml;

import java.util.UUID;

public record PredictionRequest(UUID playerId, float[][] sequence, int horizonTicks) {}
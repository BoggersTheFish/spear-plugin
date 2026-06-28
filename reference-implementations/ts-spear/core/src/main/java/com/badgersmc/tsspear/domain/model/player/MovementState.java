package com.badgersmc.tsspear.domain.model.player;

import com.badgersmc.tsspear.domain.model.sample.MovementSample;

public final class MovementState {
    private MovementSample lastSample;

    public MovementSample lastSample() {
        return lastSample;
    }

    public void recordSample(MovementSample sample) {
        this.lastSample = sample;
    }
}
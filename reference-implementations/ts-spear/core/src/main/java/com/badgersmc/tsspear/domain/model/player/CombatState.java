package com.badgersmc.tsspear.domain.model.player;

import com.badgersmc.tsspear.domain.model.sample.CombatSample;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class CombatState {
    private static final int MAX_HISTORY = 32;

    private CombatSample lastSample;
    private final Deque<CombatSample> recentHits = new ArrayDeque<>();

    public CombatSample lastSample() {
        return lastSample;
    }

    public List<CombatSample> recentHits() {
        return List.copyOf(recentHits);
    }

    public void recordHit(CombatSample sample) {
        this.lastSample = sample;
        recentHits.addLast(sample);
        while (recentHits.size() > MAX_HISTORY) {
            recentHits.removeFirst();
        }
    }
}
package com.badgersmc.tsspear.domain.service;

import com.badgersmc.tsspear.domain.model.confidence.DecayCurve;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DecayApplicatorTest {
    @Test
    void linearDecayReducesConfidence() {
        DecayApplicator applicator = new DecayApplicator();
        double result = applicator.apply(0.8, DecayCurve.LINEAR, Duration.ofSeconds(10), 0.02);
        assertTrue(result < 0.8);
    }

    @Test
    void exponentialDecayReducesConfidence() {
        DecayApplicator applicator = new DecayApplicator();
        double result = applicator.apply(0.8, DecayCurve.EXPONENTIAL, Duration.ofSeconds(10), 0.05);
        assertTrue(result < 0.8);
    }
}
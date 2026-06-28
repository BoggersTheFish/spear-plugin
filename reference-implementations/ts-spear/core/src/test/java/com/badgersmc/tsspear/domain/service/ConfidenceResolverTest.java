package com.badgersmc.tsspear.domain.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfidenceResolverTest {
    @Test
    void resolveIncreasesWithActivation() {
        ConfidenceResolver resolver = new ConfidenceResolver(1.0);
        double low = resolver.resolve(0.1, 0.0);
        double high = resolver.resolve(0.9, 0.0);
        assertTrue(high > low);
    }

    @Test
    void tensionReducesResolvedConfidence() {
        ConfidenceResolver resolver = new ConfidenceResolver(1.0);
        double withoutTension = resolver.resolve(0.8, 0.0);
        double withTension = resolver.resolve(0.8, 0.5);
        assertTrue(withoutTension > withTension);
    }
}
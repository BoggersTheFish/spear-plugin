package com.badgersmc.tsspear.application.ml;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.model.sample.PacketSample;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureExtractorTest {
    @Test
    void extractsNamedFeatureVector() {
        PlayerState state = new PlayerState(UUID.randomUUID(), "Steve");
        state.trust().vector().get(ConfidenceDimension.MOVEMENT).setConfidence(0.42);
        state.suspicion().update(0.55);
        state.networking().recordPacket(new PacketSample(
            state.uuid(),
            "Steve",
            100L,
            Instant.now(),
            "POSITION",
            WorldPosition.of("world", 1, 64, 1),
            WorldPosition.of("world", 0, 64, 0),
            1.4,
            45,
            50L
        ));

        FeatureExtractor extractor = new FeatureExtractor();
        FeatureVector vector = extractor.extract(state);

        assertEquals(FeatureExtractor.FEATURE_NAMES.length, vector.size());
        assertEquals(0.42f, vector.values()[0], 0.001f);
        assertEquals(0.55f, vector.values()[FeatureExtractor.indexOf("suspicion_risk")], 0.001f);
        assertEquals(1f, vector.values()[FeatureExtractor.indexOf("packet_count")], 0.001f);
        assertTrue(vector.values()[FeatureExtractor.indexOf("packet_max_delta")] > 1.0f);
    }
}
package com.badgersmc.tsspear.check.networking;

import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.check.CheckConfig;
import com.badgersmc.tsspear.domain.check.CheckContext;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.model.sample.PacketSample;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlinkCheckTest {
    @Test
    void emitsEvidenceWhenPacketIntervalTooShort() {
        UUID playerId = UUID.randomUUID();
        PlayerState state = new PlayerState(playerId, "Test");
        WorldPosition pos = WorldPosition.of("world", 0, 64, 0);
        PacketSample sample = new PacketSample(
            playerId, "Test", 100, Instant.now(), "POSITION",
            pos, pos, 0.0, 50, 10
        );
        CheckConfig config = new CheckConfig();
        config.setInt("blink-min-interval-ms", 40);
        CheckContext ctx = CheckContext.forPacket(state, sample, config, Instant.now());

        List<Evidence> result = new BlinkCheck().evaluate(ctx);
        assertEquals(1, result.size());
        assertEquals(EvidenceKind.BLINK, result.get(0).kind());
        assertTrue(result.get(0).severity() > 0);
    }
}
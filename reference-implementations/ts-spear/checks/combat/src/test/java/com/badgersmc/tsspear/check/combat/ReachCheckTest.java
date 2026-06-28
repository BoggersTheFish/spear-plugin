package com.badgersmc.tsspear.check.combat;

import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.check.CheckConfig;
import com.badgersmc.tsspear.domain.check.CheckContext;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.model.sample.CombatSample;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReachCheckTest {
    @Test
    void emitsEvidenceWhenDistanceExceedsLimit() {
        UUID attackerId = UUID.randomUUID();
        UUID victimId = UUID.randomUUID();
        PlayerState state = new PlayerState(attackerId, "Attacker");
        WorldPosition attackerPos = WorldPosition.of("world", 0, 64, 0);
        WorldPosition victimPos = WorldPosition.of("world", 5, 64, 0);

        CombatSample sample = new CombatSample(
            attackerId, "Attacker", victimId, "Victim",
            100, Instant.now(), attackerPos, victimPos,
            5.0, 4.0, false, 50, "DIAMOND_SWORD"
        );

        CheckConfig config = new CheckConfig();
        config.setDouble("max-reach", 3.2);
        CheckContext ctx = CheckContext.forCombat(state, sample, config, Instant.now());

        List<Evidence> result = new ReachCheck().evaluate(ctx);
        assertEquals(1, result.size());
        assertEquals(EvidenceKind.REACH_EXCESS, result.get(0).kind());
        assertTrue(result.get(0).severity() > 0);
    }

    @Test
    void noEvidenceWhenWithinReach() {
        UUID attackerId = UUID.randomUUID();
        UUID victimId = UUID.randomUUID();
        PlayerState state = new PlayerState(attackerId, "Attacker");
        WorldPosition pos = WorldPosition.of("world", 0, 64, 0);

        CombatSample sample = new CombatSample(
            attackerId, "Attacker", victimId, "Victim",
            100, Instant.now(), pos, pos,
            2.5, 4.0, false, 50, "DIAMOND_SWORD"
        );

        CheckConfig config = new CheckConfig();
        config.setDouble("max-reach", 3.2);
        CheckContext ctx = CheckContext.forCombat(state, sample, config, Instant.now());

        List<Evidence> result = new ReachCheck().evaluate(ctx);
        assertTrue(result.isEmpty());
    }
}
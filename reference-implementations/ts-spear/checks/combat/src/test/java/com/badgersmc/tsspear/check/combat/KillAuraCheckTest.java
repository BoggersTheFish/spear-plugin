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

class KillAuraCheckTest {
    @Test
    void emitsEvidenceWhenMultipleVictimsHitInWindow() {
        UUID attackerId = UUID.randomUUID();
        PlayerState state = new PlayerState(attackerId, "Attacker");
        WorldPosition pos = WorldPosition.of("world", 0, 64, 0);

        for (int i = 0; i < 3; i++) {
            UUID victimId = UUID.randomUUID();
            CombatSample hit = new CombatSample(
                attackerId, "Attacker", victimId, "Victim" + i,
                100 + i, Instant.now(), pos, pos,
                2.5, 4.0, false, 50, "DIAMOND_SWORD"
            );
            state.combat().recordHit(hit);
        }

        CombatSample current = state.combat().lastSample();
        CheckConfig config = new CheckConfig();
        config.setInt("killaura-window-ticks", 10);
        config.setInt("killaura-victim-threshold", 3);
        CheckContext ctx = CheckContext.forCombat(state, current, config, Instant.now());

        List<Evidence> result = new KillAuraCheck().evaluate(ctx);
        assertEquals(1, result.size());
        assertEquals(EvidenceKind.KILL_AURA_PATTERN, result.get(0).kind());
        assertTrue(result.get(0).severity() > 0);
    }

    @Test
    void noEvidenceWithSingleVictim() {
        UUID attackerId = UUID.randomUUID();
        UUID victimId = UUID.randomUUID();
        PlayerState state = new PlayerState(attackerId, "Attacker");
        WorldPosition pos = WorldPosition.of("world", 0, 64, 0);

        CombatSample hit = new CombatSample(
            attackerId, "Attacker", victimId, "Victim",
            100, Instant.now(), pos, pos,
            2.5, 4.0, false, 50, "DIAMOND_SWORD"
        );
        state.combat().recordHit(hit);

        CheckConfig config = new CheckConfig();
        config.setInt("killaura-window-ticks", 10);
        config.setInt("killaura-victim-threshold", 3);
        CheckContext ctx = CheckContext.forCombat(state, hit, config, Instant.now());

        List<Evidence> result = new KillAuraCheck().evaluate(ctx);
        assertTrue(result.isEmpty());
    }
}
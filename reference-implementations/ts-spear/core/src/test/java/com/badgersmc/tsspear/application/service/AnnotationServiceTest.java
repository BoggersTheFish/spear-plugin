package com.badgersmc.tsspear.application.service;

import com.badgersmc.tsspear.api.ml.AnnotationVerdict;
import com.badgersmc.tsspear.infrastructure.memory.InMemoryAnnotationStore;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationServiceTest {
    @Test
    void storesAndReturnsLatestLabel() {
        AnnotationService service = new AnnotationService(new InMemoryAnnotationStore());
        UUID playerId = UUID.randomUUID();

        service.annotate(playerId, "Staff", AnnotationVerdict.CHEATING, "obvious reach", null);
        service.annotate(playerId, "Staff2", AnnotationVerdict.LEGIT, "false positive", null);

        assertEquals(AnnotationVerdict.LEGIT, service.latestForPlayer(playerId).orElseThrow().verdict());
        assertTrue(service.labelForExport(playerId).orElseThrow().containsKey("verdict"));
    }
}
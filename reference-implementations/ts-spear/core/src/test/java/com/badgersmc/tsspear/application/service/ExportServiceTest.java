package com.badgersmc.tsspear.application.service;

import com.badgersmc.tsspear.api.ml.AnnotationVerdict;
import com.badgersmc.tsspear.application.ml.ExportTarget;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.infrastructure.memory.InMemoryAnnotationStore;
import com.badgersmc.tsspear.infrastructure.memory.InMemoryReceiptStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void exportIncludesStaffLabel() throws Exception {
        InMemoryAnnotationStore annotationStore = new InMemoryAnnotationStore();
        AnnotationService annotationService = new AnnotationService(annotationStore);
        UUID playerId = UUID.randomUUID();
        annotationService.annotate(playerId, "Mod", AnnotationVerdict.SUSPICIOUS, "weird movement", null);

        ExportService exportService = new ExportService(
            new InMemoryReceiptStore(),
            new com.badgersmc.tsspear.application.ml.FeatureExtractor(),
            new com.badgersmc.tsspear.application.ml.SequenceBuilder(),
            annotationService,
            300
        );

        PlayerState state = new PlayerState(playerId, "Alex");
        Path out = tempDir.resolve("alex.json");
        exportService.exportPlayer(state, new ExportTarget(out, ExportTarget.ExportFormat.JSON), 10);

        String json = Files.readString(out);
        assertTrue(json.contains("\"schemaVersion\""));
        assertTrue(json.contains("\"SUSPICIOUS\""));
        assertTrue(json.contains("\"features\""));
    }
}
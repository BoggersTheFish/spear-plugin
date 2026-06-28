package com.badgersmc.tsspear.storage.sqlite;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SqliteStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void storesAndLoadsReceiptsAndEvidence() {
        try (DatabaseManager db = new DatabaseManager(tempDir.resolve("test.db"))) {
            SqliteReceiptStore receipts = new SqliteReceiptStore(db.dataSource());
            SqliteEvidenceStore evidenceStore = new SqliteEvidenceStore(db.dataSource());

            UUID playerId = UUID.randomUUID();
            ConfidenceReceipt receipt = ConfidenceReceipt.create(
                playerId,
                "GRAPH_PROPAGATION",
                ConfidenceDimension.MOVEMENT,
                0.2,
                0.5,
                "test receipt",
                List.of(),
                1L,
                0.1,
                0.2,
                1,
                0,
                WorldPosition.of("world", 1, 64, 2)
            );
            receipts.store(receipt);

            Evidence evidence = Evidence.builder()
                .playerId(playerId)
                .kind(EvidenceKind.IMPOSSIBLE_ACCELERATION)
                .dimension(ConfidenceDimension.MOVEMENT)
                .severity(0.9)
                .credibility(0.95)
                .timestamp(Instant.now())
                .tick(100)
                .location(WorldPosition.of("world", 0, 64, 0))
                .reason("test evidence")
                .sourceCheckId("impossible-movement")
                .build();
            evidenceStore.store(evidence);

            assertEquals(1, receipts.findByPlayer(playerId, 10).size());
            assertFalse(evidenceStore.findByPlayer(playerId, 10).isEmpty());
        }
    }
}
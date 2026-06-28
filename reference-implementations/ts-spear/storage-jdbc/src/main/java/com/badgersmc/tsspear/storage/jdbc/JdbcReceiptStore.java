package com.badgersmc.tsspear.storage.jdbc;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.domain.model.ids.EvidenceId;
import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;
import com.badgersmc.tsspear.domain.port.ReceiptStore;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JdbcReceiptStore implements ReceiptStore {
    private final DataSource dataSource;
    private final SqlDialect dialect;

    public JdbcReceiptStore(DataSource dataSource, SqlDialect dialect) {
        this.dataSource = dataSource;
        this.dialect = dialect;
    }

    @Override
    public void store(ConfidenceReceipt receipt) {
        String sql = """
            INSERT INTO ts_receipt (
                receipt_id, player_id, event_time, world, loc_x, loc_y, loc_z,
                event_name, dimension, confidence_delta, previous_value, new_value,
                reason, state_version, tension_before, tension_after,
                supporting_count, conflicting_count
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            WorldPosition loc = receipt.location();
            ps.setString(1, receipt.receiptId().toString());
            ps.setString(2, receipt.playerId().toString());
            ps.setString(3, receipt.time().toString());
            ps.setString(4, receipt.world());
            if (loc != null) {
                ps.setDouble(5, loc.x());
                ps.setDouble(6, loc.y());
                ps.setDouble(7, loc.z());
            } else {
                ps.setNull(5, java.sql.Types.DOUBLE);
                ps.setNull(6, java.sql.Types.DOUBLE);
                ps.setNull(7, java.sql.Types.DOUBLE);
            }
            ps.setString(8, receipt.event());
            ps.setString(9, receipt.dimension().name());
            ps.setDouble(10, receipt.confidenceDelta());
            ps.setDouble(11, receipt.previousValue());
            ps.setDouble(12, receipt.newValue());
            ps.setString(13, receipt.reason());
            ps.setLong(14, receipt.stateVersion());
            ps.setDouble(15, receipt.tensionBefore());
            ps.setDouble(16, receipt.tensionAfter());
            ps.setInt(17, receipt.supportingCount());
            ps.setInt(18, receipt.conflictingCount());
            ps.executeUpdate();

            insertEvidenceLinks(conn, receipt);
        } catch (Exception ex) {
            throw new StorageException("Failed to store receipt", ex);
        }
    }

    private void insertEvidenceLinks(Connection conn, ConfidenceReceipt receipt) throws Exception {
        if (receipt.linkedEvidenceIds().isEmpty()) {
            return;
        }
        String sql = dialect.insertIgnoreReceiptEvidence();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (EvidenceId evidenceId : receipt.linkedEvidenceIds()) {
                ps.setString(1, receipt.receiptId().toString());
                ps.setString(2, evidenceId.value().toString());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public List<ConfidenceReceipt> findByPlayer(UUID playerId, int limit) {
        String sql = """
            SELECT * FROM ts_receipt WHERE player_id = ?
            ORDER BY event_time DESC LIMIT ?
            """;
        List<ConfidenceReceipt> receipts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    receipts.add(mapReceipt(rs));
                }
            }
        } catch (Exception ex) {
            throw new StorageException("Failed to query receipts", ex);
        }
        return receipts;
    }

    private static ConfidenceReceipt mapReceipt(ResultSet rs) throws Exception {
        WorldPosition loc = null;
        double lx = rs.getDouble("loc_x");
        if (!rs.wasNull()) {
            loc = WorldPosition.of(rs.getString("world"), lx, rs.getDouble("loc_y"), rs.getDouble("loc_z"));
        }
        return new ConfidenceReceipt(
            UUID.fromString(rs.getString("receipt_id")),
            UUID.fromString(rs.getString("player_id")),
            Instant.parse(rs.getString("event_time")),
            rs.getString("world"),
            loc,
            rs.getString("event_name"),
            ConfidenceDimension.valueOf(rs.getString("dimension")),
            rs.getDouble("confidence_delta"),
            rs.getDouble("previous_value"),
            rs.getDouble("new_value"),
            rs.getString("reason"),
            List.of(),
            rs.getLong("state_version"),
            rs.getDouble("tension_before"),
            rs.getDouble("tension_after"),
            rs.getInt("supporting_count"),
            rs.getInt("conflicting_count")
        );
    }
}
package com.badgersmc.tsspear.storage.sqlite;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.api.evidence.EvidenceKind;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.EvidenceId;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;
import com.badgersmc.tsspear.domain.port.EvidenceStore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class SqliteEvidenceStore implements EvidenceStore {
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private final DataSource dataSource;

    public SqliteEvidenceStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void store(Evidence evidence) {
        String sql = """
            INSERT OR IGNORE INTO ts_evidence (
                evidence_id, player_id, kind, dimension, severity, credibility, weight,
                timestamp, tick, world, loc_x, loc_y, loc_z, reason, source_check_id, payload_json
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            WorldPosition loc = evidence.location();
            ps.setString(1, evidence.id().value().toString());
            ps.setString(2, evidence.playerId().toString());
            ps.setString(3, evidence.kind().name());
            ps.setString(4, evidence.dimension().name());
            ps.setDouble(5, evidence.severity());
            ps.setDouble(6, evidence.credibility());
            ps.setDouble(7, evidence.weight());
            ps.setString(8, evidence.timestamp().toString());
            ps.setLong(9, evidence.tick());
            if (loc != null) {
                ps.setString(10, loc.world());
                ps.setDouble(11, loc.x());
                ps.setDouble(12, loc.y());
                ps.setDouble(13, loc.z());
            } else {
                ps.setNull(10, java.sql.Types.VARCHAR);
                ps.setNull(11, java.sql.Types.DOUBLE);
                ps.setNull(12, java.sql.Types.DOUBLE);
                ps.setNull(13, java.sql.Types.DOUBLE);
            }
            ps.setString(14, evidence.reason());
            ps.setString(15, evidence.sourceCheckId());
            ps.setString(16, MAPPER.writeValueAsString(evidence.payload()));
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new StorageException("Failed to store evidence", ex);
        }
    }

    @Override
    public List<Evidence> findByPlayer(UUID playerId, int limit) {
        String sql = "SELECT * FROM ts_evidence WHERE player_id = ? ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                return mapAll(rs);
            }
        } catch (Exception ex) {
            throw new StorageException("Failed to query evidence", ex);
        }
    }

    @Override
    public Optional<Evidence> findById(EvidenceId id) {
        String sql = "SELECT * FROM ts_evidence WHERE evidence_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.value().toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<Evidence> list = mapAll(rs);
                return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
            }
        } catch (Exception ex) {
            throw new StorageException("Failed to find evidence", ex);
        }
    }

    private List<Evidence> mapAll(ResultSet rs) throws Exception {
        List<Evidence> list = new java.util.ArrayList<>();
        while (rs.next()) {
            list.add(mapEvidence(rs));
        }
        return list;
    }

    private Evidence mapEvidence(ResultSet rs) throws Exception {
        WorldPosition loc = null;
        String world = rs.getString("world");
        double lx = rs.getDouble("loc_x");
        if (world != null && !rs.wasNull()) {
            loc = WorldPosition.of(world, lx, rs.getDouble("loc_y"), rs.getDouble("loc_z"));
        }
        String payloadJson = rs.getString("payload_json");
        Map<String, Object> payload = payloadJson == null
            ? Map.of()
            : MAPPER.readValue(payloadJson, new TypeReference<>() {});
        return new Evidence(
            EvidenceId.of(UUID.fromString(rs.getString("evidence_id"))),
            UUID.fromString(rs.getString("player_id")),
            EvidenceKind.valueOf(rs.getString("kind")),
            ConfidenceDimension.valueOf(rs.getString("dimension")),
            rs.getDouble("severity"),
            rs.getDouble("credibility"),
            java.time.Instant.parse(rs.getString("timestamp")),
            rs.getLong("tick"),
            loc,
            rs.getString("reason"),
            rs.getString("source_check_id"),
            payload,
            Set.of()
        );
    }
}
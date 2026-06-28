package com.badgersmc.tsspear.storage.jdbc;

import com.badgersmc.tsspear.domain.model.replay.ReplaySequence;
import com.badgersmc.tsspear.domain.port.ReplayStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;

public final class JdbcReplayStore implements ReplayStore {
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private final DataSource dataSource;

    public JdbcReplayStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void store(ReplaySequence sequence) {
        String sql = """
            INSERT INTO ts_replay (
                sequence_id, player_id, player_name, start_tick, end_tick,
                start_time, trigger_reason, frame_count, json_export
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sequence.sequenceId().toString());
            ps.setString(2, sequence.playerId().toString());
            ps.setString(3, sequence.playerName());
            ps.setLong(4, sequence.startTick());
            ps.setLong(5, sequence.endTick());
            ps.setString(6, sequence.startTime().toString());
            ps.setString(7, sequence.triggerReason().name());
            ps.setInt(8, sequence.frames().size());
            ps.setString(9, MAPPER.writeValueAsString(sequence));
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new StorageException("Failed to store replay", ex);
        }
    }

    @Override
    public Optional<ReplaySequence> findById(UUID sequenceId) {
        String sql = "SELECT json_export FROM ts_replay WHERE sequence_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sequenceId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(MAPPER.readValue(rs.getString("json_export"), ReplaySequence.class));
            }
        } catch (Exception ex) {
            throw new StorageException("Failed to load replay", ex);
        }
    }

    @Override
    public Optional<ReplaySequence> findLatestForPlayer(UUID playerId) {
        String sql = "SELECT json_export FROM ts_replay WHERE player_id = ? ORDER BY start_time DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(MAPPER.readValue(rs.getString("json_export"), ReplaySequence.class));
            }
        } catch (Exception ex) {
            throw new StorageException("Failed to load latest replay", ex);
        }
    }
}
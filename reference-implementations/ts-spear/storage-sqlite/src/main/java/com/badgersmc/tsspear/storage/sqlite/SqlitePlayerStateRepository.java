package com.badgersmc.tsspear.storage.sqlite;

import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.port.PlayerStateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class SqlitePlayerStateRepository implements PlayerStateRepository {
    private final ObjectMapper mapper = new ObjectMapper();

    private final DataSource dataSource;

    public SqlitePlayerStateRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<PlayerState> findById(UUID playerId) {
        String sql = """
            SELECT snapshot_json FROM ts_player_state
            WHERE player_id = ? ORDER BY captured_at DESC LIMIT 1
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                String json = rs.getString("snapshot_json");
                PlayerStateSnapshot snap = mapper.readValue(json, PlayerStateSnapshot.class);
                PlayerState state = new PlayerState(snap.playerId(), snap.name());
                state.suspicion().update(snap.suspicionRisk());
                return Optional.of(state);
            }
        } catch (Exception ex) {
            throw new StorageException("Failed to load player state", ex);
        }
    }

    @Override
    public void save(PlayerState state) {
        String sql = """
            INSERT INTO ts_player_state (player_id, version, snapshot_json, trust_json, suspicion_json, captured_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            PlayerStateSnapshot snap = PlayerStateSnapshot.from(state);
            ObjectNode trust = mapper.createObjectNode();
            state.trust().vector().asMap().forEach((dim, dc) ->
                trust.put(dim.name(), dc.confidence())
            );
            ObjectNode suspicion = mapper.createObjectNode();
            suspicion.put("current", state.suspicion().currentRisk());
            suspicion.put("previous", state.suspicion().previousRisk());

            ps.setString(1, state.uuid().toString());
            ps.setLong(2, state.version());
            ps.setString(3, mapper.writeValueAsString(snap));
            ps.setString(4, mapper.writeValueAsString(trust));
            ps.setString(5, mapper.writeValueAsString(suspicion));
            ps.setString(6, Instant.now().toString());
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new StorageException("Failed to save player state", ex);
        }
    }

    @Override
    public void delete(UUID playerId) {
        // Retain history — no-op for MVP
    }

    record PlayerStateSnapshot(UUID playerId, String name, double suspicionRisk) {
        static PlayerStateSnapshot from(PlayerState state) {
            return new PlayerStateSnapshot(state.uuid(), state.name(), state.suspicion().currentRisk());
        }
    }
}
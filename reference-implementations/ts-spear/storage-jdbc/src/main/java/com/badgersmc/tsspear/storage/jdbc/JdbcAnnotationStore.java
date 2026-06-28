package com.badgersmc.tsspear.storage.jdbc;

import com.badgersmc.tsspear.api.ml.AnnotationVerdict;
import com.badgersmc.tsspear.domain.model.annotation.StaffAnnotation;
import com.badgersmc.tsspear.domain.port.AnnotationStore;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class JdbcAnnotationStore implements AnnotationStore {
    private final DataSource dataSource;

    public JdbcAnnotationStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void store(StaffAnnotation annotation) {
        String sql = """
            INSERT INTO ts_staff_annotation (
                annotation_id, player_id, staff_name, verdict, notes, annotated_at, linked_export_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, annotation.annotationId().toString());
            ps.setString(2, annotation.playerId().toString());
            ps.setString(3, annotation.staffName());
            ps.setString(4, annotation.verdict().name());
            ps.setString(5, annotation.notes());
            ps.setString(6, annotation.annotatedAt().toString());
            if (annotation.linkedExportId() != null) {
                ps.setString(7, annotation.linkedExportId().toString());
            } else {
                ps.setNull(7, java.sql.Types.CHAR);
            }
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new StorageException("Failed to store annotation", ex);
        }
    }

    @Override
    public Optional<StaffAnnotation> findLatestForPlayer(UUID playerId) {
        return findByPlayer(playerId, 1).stream().findFirst();
    }

    @Override
    public List<StaffAnnotation> findByPlayer(UUID playerId, int limit) {
        String sql = """
            SELECT annotation_id, player_id, staff_name, verdict, notes, annotated_at, linked_export_id
            FROM ts_staff_annotation
            WHERE player_id = ?
            ORDER BY annotated_at DESC
            LIMIT ?
            """;
        List<StaffAnnotation> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (Exception ex) {
            throw new StorageException("Failed to query annotations", ex);
        }
        return results;
    }

    private StaffAnnotation mapRow(ResultSet rs) throws java.sql.SQLException {
        String linked = rs.getString("linked_export_id");
        return new StaffAnnotation(
            UUID.fromString(rs.getString("annotation_id")),
            UUID.fromString(rs.getString("player_id")),
            rs.getString("staff_name"),
            AnnotationVerdict.valueOf(rs.getString("verdict")),
            rs.getString("notes"),
            Instant.parse(rs.getString("annotated_at")),
            linked == null ? null : UUID.fromString(linked)
        );
    }
}
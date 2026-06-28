package com.badgersmc.tsspear.storage.jdbc;

import com.badgersmc.tsspear.application.RuntimeDependencies;
import com.badgersmc.tsspear.application.config.StorageConnectionConfig;

import javax.sql.DataSource;

public final class JdbcStorageFactory {
    private JdbcStorageFactory() {}

    public record JdbcStorageBundle(RuntimeDependencies dependencies, JdbcDatabaseManager databaseManager)
        implements AutoCloseable {

        @Override
        public void close() {
            databaseManager.close();
        }
    }

    public static JdbcStorageBundle mysql(StorageConnectionConfig config) {
        return createBundle(config, SqlDialect.MYSQL);
    }

    public static JdbcStorageBundle postgres(StorageConnectionConfig config) {
        return createBundle(config, SqlDialect.POSTGRESQL);
    }

    private static JdbcStorageBundle createBundle(StorageConnectionConfig config, SqlDialect dialect) {
        String jdbcUrl = jdbcUrl(config, dialect);
        JdbcDatabaseManager databaseManager = new JdbcDatabaseManager(
            jdbcUrl,
            config.username(),
            config.password(),
            dialect
        );
        RuntimeDependencies dependencies = createDependencies(databaseManager.dataSource(), dialect);
        return new JdbcStorageBundle(dependencies, databaseManager);
    }

    private static String jdbcUrl(StorageConnectionConfig config, SqlDialect dialect) {
        return switch (dialect) {
            case MYSQL -> "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
                .formatted(config.host(), config.port(), config.database());
            case POSTGRESQL -> "jdbc:postgresql://%s:%d/%s"
                .formatted(config.host(), config.port(), config.database());
        };
    }

    private static RuntimeDependencies createDependencies(DataSource dataSource, SqlDialect dialect) {
        return new RuntimeDependencies(
            new JdbcPlayerStateRepository(dataSource),
            new JdbcReceiptStore(dataSource, dialect),
            new JdbcEvidenceStore(dataSource, dialect),
            new JdbcReplayStore(dataSource),
            new JdbcAnnotationStore(dataSource)
        );
    }
}
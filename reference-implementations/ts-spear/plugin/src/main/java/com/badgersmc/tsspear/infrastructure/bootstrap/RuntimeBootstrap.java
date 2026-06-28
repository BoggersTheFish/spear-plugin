package com.badgersmc.tsspear.infrastructure.bootstrap;

import com.badgersmc.tsspear.application.RuntimeDependencies;
import com.badgersmc.tsspear.application.TSSpearRuntime;
import com.badgersmc.tsspear.application.config.DecaySettings;
import com.badgersmc.tsspear.application.config.StorageConnectionConfig;
import com.badgersmc.tsspear.application.config.TSSpearSettings;
import com.badgersmc.tsspear.domain.check.Check;
import com.badgersmc.tsspear.infrastructure.memory.InMemoryAnnotationStore;
import com.badgersmc.tsspear.infrastructure.memory.InMemoryEvidenceStore;
import com.badgersmc.tsspear.infrastructure.memory.InMemoryPlayerStateRepository;
import com.badgersmc.tsspear.infrastructure.memory.InMemoryReceiptStore;
import com.badgersmc.tsspear.infrastructure.memory.InMemoryReplayStore;
import com.badgersmc.tsspear.storage.sqlite.SqliteAnnotationStore;
import com.badgersmc.tsspear.storage.jdbc.JdbcStorageFactory;
import com.badgersmc.tsspear.storage.sqlite.DatabaseManager;
import com.badgersmc.tsspear.storage.sqlite.SqliteEvidenceStore;
import com.badgersmc.tsspear.storage.sqlite.SqlitePlayerStateRepository;
import com.badgersmc.tsspear.storage.sqlite.SqliteReceiptStore;
import com.badgersmc.tsspear.storage.sqlite.SqliteReplayStore;

import java.nio.file.Path;
import java.util.List;

public final class RuntimeBootstrap implements AutoCloseable {
    private final TSSpearRuntime runtime;
    private final AutoCloseable storageResource;

    private RuntimeBootstrap(TSSpearRuntime runtime, AutoCloseable storageResource) {
        this.runtime = runtime;
        this.storageResource = storageResource;
    }

    public static RuntimeBootstrap create(
        TSSpearSettings settings,
        DecaySettings decaySettings,
        Path dataFolder,
        String storageBackend,
        StorageConnectionConfig mysqlConfig,
        StorageConnectionConfig postgresConfig,
        List<Check> checks
    ) {
        String backend = storageBackend == null ? "SQLITE" : storageBackend.toUpperCase();
        return switch (backend) {
            case "MYSQL" -> {
                JdbcStorageFactory.JdbcStorageBundle bundle = JdbcStorageFactory.mysql(mysqlConfig);
                yield new RuntimeBootstrap(new TSSpearRuntime(settings, decaySettings, bundle.dependencies(), checks), bundle);
            }
            case "POSTGRESQL", "POSTGRES" -> {
                JdbcStorageFactory.JdbcStorageBundle bundle = JdbcStorageFactory.postgres(postgresConfig);
                yield new RuntimeBootstrap(new TSSpearRuntime(settings, decaySettings, bundle.dependencies(), checks), bundle);
            }
            case "MEMORY" -> {
                RuntimeDependencies deps = memoryDependencies();
                yield new RuntimeBootstrap(new TSSpearRuntime(settings, decaySettings, deps, checks), null);
            }
            default -> {
                Path dbFile = dataFolder.resolve("tsspear.db");
                DatabaseManager db = new DatabaseManager(dbFile);
                RuntimeDependencies deps = new RuntimeDependencies(
                    new SqlitePlayerStateRepository(db.dataSource()),
                    new SqliteReceiptStore(db.dataSource()),
                    new SqliteEvidenceStore(db.dataSource()),
                    new SqliteReplayStore(db.dataSource()),
                    new SqliteAnnotationStore(db.dataSource())
                );
                yield new RuntimeBootstrap(new TSSpearRuntime(settings, decaySettings, deps, checks), db);
            }
        };
    }

    private static RuntimeDependencies memoryDependencies() {
        return new RuntimeDependencies(
            new InMemoryPlayerStateRepository(),
            new InMemoryReceiptStore(),
            new InMemoryEvidenceStore(),
            new InMemoryReplayStore(),
            new InMemoryAnnotationStore()
        );
    }

    public TSSpearRuntime runtime() {
        return runtime;
    }

    @Override
    public void close() {
        if (storageResource != null) {
            try {
                storageResource.close();
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to close storage resource", ex);
            }
        }
    }
}
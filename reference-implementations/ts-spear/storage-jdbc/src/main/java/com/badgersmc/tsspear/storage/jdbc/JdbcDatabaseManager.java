package com.badgersmc.tsspear.storage.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public final class JdbcDatabaseManager implements AutoCloseable {
    private final HikariDataSource dataSource;

    public JdbcDatabaseManager(String jdbcUrl, String username, String password, SqlDialect dialect) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(8);
        config.setMinimumIdle(1);
        config.setPoolName("ts-spear-" + dialect.name().toLowerCase());
        this.dataSource = new HikariDataSource(config);
        migrate(dialect);
    }

    private void migrate(SqlDialect dialect) {
        Flyway.configure()
            .dataSource(dataSource)
            .locations(dialect.migrationLocation())
            .load()
            .migrate();
    }

    public DataSource dataSource() {
        return dataSource;
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
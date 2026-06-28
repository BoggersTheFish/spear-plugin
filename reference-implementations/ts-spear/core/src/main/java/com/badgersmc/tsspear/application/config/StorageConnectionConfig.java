package com.badgersmc.tsspear.application.config;

public record StorageConnectionConfig(
    String host,
    int port,
    String database,
    String username,
    String password
) {
    public static StorageConnectionConfig defaults() {
        return new StorageConnectionConfig("localhost", 3306, "tsspear", "root", "");
    }
}
package com.badgersmc.tsspear.domain.model.world;

public record WorldPosition(
    String world,
    double x,
    double y,
    double z,
    float yaw,
    float pitch
) {
    public static WorldPosition of(String world, double x, double y, double z) {
        return new WorldPosition(world, x, y, z, 0f, 0f);
    }
}
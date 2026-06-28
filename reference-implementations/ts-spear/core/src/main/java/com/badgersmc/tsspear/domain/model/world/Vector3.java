package com.badgersmc.tsspear.domain.model.world;

public record Vector3(double x, double y, double z) {
    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static Vector3 zero() {
        return new Vector3(0, 0, 0);
    }

    public static Vector3 between(WorldPosition from, WorldPosition to) {
        return new Vector3(to.x() - from.x(), to.y() - from.y(), to.z() - from.z());
    }
}
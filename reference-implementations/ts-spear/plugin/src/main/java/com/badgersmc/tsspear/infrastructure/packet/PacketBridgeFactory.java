package com.badgersmc.tsspear.infrastructure.packet;

import com.badgersmc.tsspear.application.TSSpearRuntime;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class PacketBridgeFactory {
    private PacketBridgeFactory() {}

    public static PacketSamplePublisher create(JavaPlugin plugin, TSSpearRuntime runtime) {
        PacketSamplePublisher publisher = new PacketSamplePublisher(runtime);
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            try {
                ProtocolLibPacketBridge.register(plugin, publisher);
                plugin.getLogger().info("TS-Spear: ProtocolLib packet bridge active");
            } catch (Throwable ex) {
                plugin.getLogger().warning("TS-Spear: ProtocolLib present but bridge failed: " + ex.getMessage());
            }
        } else {
            plugin.getLogger().info("TS-Spear: movement-based packet sampling (ProtocolLib not installed)");
        }
        return publisher;
    }
}
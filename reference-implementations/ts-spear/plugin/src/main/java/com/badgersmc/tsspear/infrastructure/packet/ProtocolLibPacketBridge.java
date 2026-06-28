package com.badgersmc.tsspear.infrastructure.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProtocolLibPacketBridge {
    private ProtocolLibPacketBridge() {}

    public static void register(JavaPlugin plugin, PacketSamplePublisher publisher) {
        ProtocolLibrary.getProtocolManager().addPacketListener(
            new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Client.POSITION,
                PacketType.Play.Client.POSITION_LOOK) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    if (!(event.getPlayer() instanceof Player player)) {
                        return;
                    }
                    Location server = player.getLocation();
                    double reportedX = event.getPacket().getDoubles().read(0);
                    double reportedY = event.getPacket().getDoubles().read(1);
                    double reportedZ = event.getPacket().getDoubles().read(2);
                    WorldPosition reported = WorldPosition.of(
                        server.getWorld().getName(), reportedX, reportedY, reportedZ
                    );
                    WorldPosition serverPos = WorldPosition.of(
                        server.getWorld().getName(), server.getX(), server.getY(), server.getZ()
                    );
                    double delta = Math.sqrt(
                        Math.pow(reportedX - server.getX(), 2)
                            + Math.pow(reportedY - server.getY(), 2)
                            + Math.pow(reportedZ - server.getZ(), 2)
                    );
                    publisher.publish(player, event.getPacketType().name(), reported, serverPos, delta);
                }
            }
        );
    }
}
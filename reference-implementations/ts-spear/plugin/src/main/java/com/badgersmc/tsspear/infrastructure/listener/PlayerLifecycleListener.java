package com.badgersmc.tsspear.infrastructure.listener;

import com.badgersmc.tsspear.application.TSSpearRuntime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerLifecycleListener implements Listener {
    private final TSSpearRuntime runtime;

    public PlayerLifecycleListener(TSSpearRuntime runtime) {
        this.runtime = runtime;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        runtime.stateEngine().getOrCreate(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();
        runtime.replayEngine().clear(uuid);
        runtime.stateEngine().remove(uuid);
    }
}
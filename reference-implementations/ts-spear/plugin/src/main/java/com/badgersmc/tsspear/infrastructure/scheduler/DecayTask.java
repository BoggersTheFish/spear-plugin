package com.badgersmc.tsspear.infrastructure.scheduler;

import com.badgersmc.tsspear.application.TSSpearRuntime;
import org.bukkit.scheduler.BukkitRunnable;

public final class DecayTask extends BukkitRunnable {
    private final TSSpearRuntime runtime;

    public DecayTask(TSSpearRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void run() {
        runtime.decayScheduler().tick();
    }
}
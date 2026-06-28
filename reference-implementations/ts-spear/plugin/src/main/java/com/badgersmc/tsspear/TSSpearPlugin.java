package com.badgersmc.tsspear;

import com.badgersmc.tsspear.application.config.DecaySettings;
import com.badgersmc.tsspear.application.config.TSSpearSettings;
import com.badgersmc.tsspear.check.combat.KillAuraCheck;
import com.badgersmc.tsspear.check.combat.ReachCheck;
import com.badgersmc.tsspear.check.combat.VelocityCheck;
import com.badgersmc.tsspear.check.movement.FlyCheck;
import com.badgersmc.tsspear.check.movement.ImpossibleMovementCheck;
import com.badgersmc.tsspear.check.movement.LagSpikeMonitor;
import com.badgersmc.tsspear.check.movement.SpeedCheck;
import com.badgersmc.tsspear.check.networking.BadPacketCheck;
import com.badgersmc.tsspear.check.networking.BlinkCheck;
import com.badgersmc.tsspear.infrastructure.bootstrap.RuntimeBootstrap;
import com.badgersmc.tsspear.infrastructure.command.TSSpearCommand;
import com.badgersmc.tsspear.infrastructure.config.PluginConfigLoader;
import com.badgersmc.tsspear.infrastructure.gui.GuiListener;
import com.badgersmc.tsspear.infrastructure.gui.GuiManager;
import com.badgersmc.tsspear.infrastructure.i18n.MessageService;
import com.badgersmc.tsspear.infrastructure.listener.CombatListener;
import com.badgersmc.tsspear.infrastructure.listener.MovementListener;
import com.badgersmc.tsspear.infrastructure.packet.PacketBridgeFactory;
import com.badgersmc.tsspear.infrastructure.listener.PlayerLifecycleListener;
import com.badgersmc.tsspear.infrastructure.notification.StaffAlertNotifier;
import com.badgersmc.tsspear.infrastructure.scheduler.DecayTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;

public final class TSSpearPlugin extends JavaPlugin {
    private RuntimeBootstrap bootstrap;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveDefaultMessages();
        TSSpearSettings settings = PluginConfigLoader.load(getConfig());
        DecaySettings decaySettings = PluginConfigLoader.loadDecay(getConfig());
        String storageBackend = PluginConfigLoader.storageBackend(getConfig());
        String locale = PluginConfigLoader.locale(getConfig());

        bootstrap = RuntimeBootstrap.create(
            settings,
            decaySettings,
            getDataFolder().toPath(),
            storageBackend,
            PluginConfigLoader.loadMysql(getConfig()),
            PluginConfigLoader.loadPostgres(getConfig()),
            List.of(
                new ImpossibleMovementCheck(),
                new LagSpikeMonitor(),
                new FlyCheck(),
                new SpeedCheck(),
                new ReachCheck(),
                new KillAuraCheck(),
                new VelocityCheck(),
                new BadPacketCheck(),
                new BlinkCheck()
            )
        );
        var runtime = bootstrap.runtime();
        runtime.start();

        MessageService messages = new MessageService(this, locale);
        StaffAlertNotifier staffAlertNotifier = new StaffAlertNotifier(runtime);
        runtime.notificationEngine().setAlertSink(staffAlertNotifier::notify);

        GuiManager guiManager = new GuiManager(runtime);
        var packetPublisher = PacketBridgeFactory.create(this, runtime);
        var pm = getServer().getPluginManager();
        pm.registerEvents(new MovementListener(runtime, packetPublisher), this);
        pm.registerEvents(new CombatListener(runtime), this);
        pm.registerEvents(new PlayerLifecycleListener(runtime), this);
        pm.registerEvents(new GuiListener(guiManager), this);

        TSSpearCommand command = new TSSpearCommand(runtime, guiManager, messages, getDataFolder().toPath());
        var ts = getCommand("ts");
        if (ts != null) {
            ts.setExecutor(command);
            ts.setTabCompleter(command);
        } else {
            getLogger().severe("Command 'ts' missing from plugin.yml");
        }

        new DecayTask(runtime).runTaskTimer(this, 20L, 20L);

        getLogger().info("TS-Spear enabled — storage=" + storageBackend
            + ", replay=" + settings.replayRingBufferSeconds() + "s + "
            + settings.continuousCaptureSeconds() + "s continuous"
            + ", locale=" + locale);
    }

    private void saveDefaultMessages() {
        var messagesDir = getDataFolder().toPath().resolve("messages");
        try {
            Files.createDirectories(messagesDir);
            var target = messagesDir.resolve("en_US.yml");
            if (!Files.exists(target) && getResource("messages/en_US.yml") != null) {
                saveResource("messages/en_US.yml", false);
            }
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, "Could not create messages directory", ex);
        }
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            try {
                bootstrap.runtime().shutdown();
                bootstrap.close();
            } catch (Exception ex) {
                getLogger().log(Level.WARNING, "Error during TS-Spear shutdown", ex);
            }
        }
        getLogger().info("TS-Spear disabled");
    }

    public RuntimeBootstrap bootstrap() {
        return bootstrap;
    }
}
package com.badgersmc.tsspear.infrastructure.config;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.application.config.DecaySettings;
import com.badgersmc.tsspear.application.config.StorageConnectionConfig;
import com.badgersmc.tsspear.application.config.TSSpearSettings;
import com.badgersmc.tsspear.application.scheduler.DecayScheduler;
import com.badgersmc.tsspear.domain.model.confidence.DecayCurve;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Map;

public final class PluginConfigLoader {
    private PluginConfigLoader() {}

    public static TSSpearSettings load(FileConfiguration config) {
        TSSpearSettings d = TSSpearSettings.defaults();
        return new TSSpearSettings(
            config.getDouble("checks.movement.max-acceleration", d.maxAcceleration()),
            config.getDouble("checks.movement.max-speed", d.maxSpeed()),
            config.getDouble("checks.combat.max-reach", d.maxReach()),
            config.getInt("checks.movement.max-air-ticks", d.maxAirTicks()),
            config.getInt("checks.combat.killaura-window-ticks", d.killAuraWindowTicks()),
            config.getInt("checks.combat.killaura-victim-threshold", d.killAuraVictimThreshold()),
            config.getDouble("checks.combat.max-knockback-damage-ratio", d.maxKnockbackDamageRatio()),
            config.getInt("networking.lag-threshold-ms", d.lagThresholdMs()),
            config.getDouble("graph.contradiction-multiplier", d.contradictionMultiplier()),
            config.getDouble("graph.tension-sensitivity", d.tensionSensitivity()),
            config.getDouble("thresholds.alert", d.alertThreshold()),
            config.getDouble("graph.confidence-epsilon", d.confidenceEpsilon()),
            config.getDouble("storage.evidence-persist-threshold", d.evidencePersistThreshold()),
            config.getInt("engine.worker-threads", d.workerThreads()),
            config.getLong("engine.evidence-dedupe-window-ms", d.evidenceDedupeWindowMs()),
            config.getInt("storage.batch-size", d.storageBatchSize()),
            config.getLong("storage.flush-interval-ms", d.storageFlushIntervalMs()),
            config.getInt("replay.ring-buffer-seconds", d.replayRingBufferSeconds()),
            config.getInt("replay.ticks-per-second", d.replayTicksPerSecond()),
            config.getInt("replay.continuous-capture-seconds", d.continuousCaptureSeconds()),
            config.getBoolean("performance.object-pooling-enabled", d.objectPoolingEnabled()),
            config.getInt("performance.graph-node-pool-size", d.graphNodePoolSize()),
            config.getInt("performance.evidence-builder-pool-size", d.evidenceBuilderPoolSize()),
            config.getInt("performance.movement-scratch-pool-size", d.movementScratchPoolSize()),
            config.getInt("performance.graph-propagation-batch-size", d.graphPropagationBatchSize()),
            config.getInt("checks.networking.blink-min-interval-ms", d.blinkMinIntervalMs()),
            config.getDouble("checks.networking.bad-packet-position-threshold", d.badPacketPositionThreshold()),
            config.getInt("ml.export-window-seconds", d.mlExportWindowSeconds()),
            config.getString("ml.inference-provider", d.mlInferenceProvider()),
            config.getDouble("ml.rule-based.cheating-threshold", d.mlRuleBasedCheatingThreshold()),
            config.getDouble("ml.rule-based.suspicious-threshold", d.mlRuleBasedSuspiciousThreshold())
        );
    }

    public static DecaySettings loadDecay(FileConfiguration config) {
        Map<ConfidenceDimension, DecayScheduler.DecayCurveConfig> map = new EnumMap<>(ConfidenceDimension.class);
        for (ConfidenceDimension dim : ConfidenceDimension.values()) {
            if (dim == ConfidenceDimension.AGGREGATE) continue;
            String path = "decay." + dim.name().toLowerCase() + ".";
            map.put(dim, new DecayScheduler.DecayCurveConfig(
                parseCurve(config.getString(path + "curve", "EXPONENTIAL")),
                config.getDouble(path + "rate", 0.02)
            ));
        }
        return new DecaySettings(map);
    }

    public static String storageBackend(FileConfiguration config) {
        return config.getString("storage.backend", "SQLITE");
    }

    public static StorageConnectionConfig loadMysql(FileConfiguration config) {
        StorageConnectionConfig d = StorageConnectionConfig.defaults();
        return new StorageConnectionConfig(
            config.getString("storage.mysql.host", d.host()),
            config.getInt("storage.mysql.port", d.port()),
            config.getString("storage.mysql.database", d.database()),
            config.getString("storage.mysql.username", d.username()),
            config.getString("storage.mysql.password", d.password())
        );
    }

    public static StorageConnectionConfig loadPostgres(FileConfiguration config) {
        return new StorageConnectionConfig(
            config.getString("storage.postgres.host", "localhost"),
            config.getInt("storage.postgres.port", 5432),
            config.getString("storage.postgres.database", "tsspear"),
            config.getString("storage.postgres.username", "postgres"),
            config.getString("storage.postgres.password", "")
        );
    }

    public static String locale(FileConfiguration config) {
        return config.getString("locale", "en_US");
    }

    private static DecayCurve parseCurve(String value) {
        try { return DecayCurve.valueOf(value.toUpperCase()); }
        catch (IllegalArgumentException ex) { return DecayCurve.EXPONENTIAL; }
    }
}
package com.badgersmc.tsspear.application.engine;

import com.badgersmc.tsspear.api.check.CheckCategory;
import com.badgersmc.tsspear.application.bus.CombatSampleEvent;
import com.badgersmc.tsspear.application.bus.EventBus;
import com.badgersmc.tsspear.application.bus.EvidenceProducedEvent;
import com.badgersmc.tsspear.application.bus.PacketSampleEvent;
import com.badgersmc.tsspear.application.bus.PlayerSampleEvent;
import com.badgersmc.tsspear.application.metrics.PerformanceMetrics;
import com.badgersmc.tsspear.domain.check.Check;
import com.badgersmc.tsspear.domain.check.CheckConfig;
import com.badgersmc.tsspear.domain.check.CheckContext;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.CheckId;
import com.badgersmc.tsspear.domain.model.player.PlayerState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CheckEngine {
    private static final Logger LOGGER = Logger.getLogger(CheckEngine.class.getName());

    private final Map<CheckId, Check> checks = new LinkedHashMap<>();
    private final StateEngine stateEngine;
    private final EventBus eventBus;
    private final CheckConfig checkConfig;
    private final ExecutorService workers;
    private final PerformanceMetrics metrics;

    public CheckEngine(StateEngine stateEngine, EventBus eventBus, CheckConfig config,
                       int workerThreads, PerformanceMetrics metrics) {
        this.stateEngine = stateEngine;
        this.eventBus = eventBus;
        this.checkConfig = config;
        this.metrics = metrics;
        this.workers = Executors.newFixedThreadPool(Math.max(1, workerThreads), r -> {
            Thread t = new Thread(r, "ts-spear-check-worker");
            t.setDaemon(true);
            return t;
        });
    }

    public void register(Check check) {
        checks.put(check.id(), check);
    }

    public void start() {
        eventBus.subscribe(PlayerSampleEvent.class, this::onMovementSample);
        eventBus.subscribe(CombatSampleEvent.class, this::onCombatSample);
        eventBus.subscribe(PacketSampleEvent.class, this::onPacketSample);
    }

    public void shutdown() {
        workers.shutdownNow();
    }

    private void onMovementSample(PlayerSampleEvent event) {
        workers.execute(() -> runWithMetrics(() -> {
            PlayerState state = stateEngine.getOrCreate(
                event.sample().playerId(),
                event.sample().playerId().toString()
            );
            state.movement().recordSample(event.sample());
            CheckContext ctx = CheckContext.forMovement(state, event.sample(), checkConfig, Instant.now());
            publishEvidence(state, runChecks(ctx, CheckCategory.MOVEMENT, CheckCategory.NETWORKING));
        }));
    }

    private void onCombatSample(CombatSampleEvent event) {
        workers.execute(() -> runWithMetrics(() -> {
            PlayerState state = stateEngine.getOrCreate(
                event.sample().attackerId(),
                event.sample().attackerName()
            );
            state.combat().recordHit(event.sample());
            CheckContext ctx = CheckContext.forCombat(state, event.sample(), checkConfig, Instant.now());
            publishEvidence(state, runChecks(ctx, CheckCategory.COMBAT));
        }));
    }

    private void onPacketSample(PacketSampleEvent event) {
        workers.execute(() -> runWithMetrics(() -> {
            PlayerState state = stateEngine.getOrCreate(
                event.sample().playerId(),
                event.sample().playerName()
            );
            state.networking().recordPacket(event.sample());
            CheckContext ctx = CheckContext.forPacket(state, event.sample(), checkConfig, Instant.now());
            publishEvidence(state, runChecks(ctx, CheckCategory.NETWORKING));
        }));
    }

    private void runWithMetrics(Runnable task) {
        if (metrics != null) {
            metrics.workerTaskStarted();
        }
        long start = System.nanoTime();
        try {
            task.run();
        } finally {
            if (metrics != null) {
                metrics.recordCheck(System.nanoTime() - start);
                metrics.workerTaskFinished();
            }
        }
    }

    private List<Evidence> runChecks(CheckContext ctx, CheckCategory... categories) {
        List<Evidence> produced = new ArrayList<>();
        for (Check check : checks.values()) {
            if (!matchesCategory(check, categories)) {
                continue;
            }
            if (!check.isEnabled(checkConfig)) {
                continue;
            }
            try {
                produced.addAll(check.evaluate(ctx));
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Check failed: " + check.id().value(), ex);
            }
        }
        return produced;
    }

    private static boolean matchesCategory(Check check, CheckCategory... categories) {
        for (CheckCategory category : categories) {
            if (check.category() == category) {
                return true;
            }
        }
        return false;
    }

    private void publishEvidence(PlayerState state, List<Evidence> produced) {
        if (!produced.isEmpty()) {
            eventBus.publish(new EvidenceProducedEvent(state.uuid(), produced, Instant.now()));
        }
    }

    public List<Check> registeredChecks() {
        return List.copyOf(checks.values());
    }

    public CheckConfig checkConfig() {
        return checkConfig;
    }

    public void setCheckEnabled(String checkId, boolean enabled) {
        checkConfig.setEnabled(checkId, enabled);
    }

    public boolean isCheckEnabled(String checkId) {
        return checkConfig.isEnabled(checkId);
    }
}
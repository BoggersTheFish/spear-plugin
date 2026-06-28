package com.badgersmc.tsspear.application.scheduler;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.application.bus.ConfidenceChangedEvent;
import com.badgersmc.tsspear.application.bus.EventBus;
import com.badgersmc.tsspear.application.config.DecaySettings;
import com.badgersmc.tsspear.application.config.TSSpearSettings;
import com.badgersmc.tsspear.application.engine.StateEngine;
import com.badgersmc.tsspear.domain.model.confidence.DecayCurve;
import com.badgersmc.tsspear.domain.model.confidence.DimensionConfidence;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;
import com.badgersmc.tsspear.domain.service.DecayApplicator;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

public final class DecayScheduler {
    private final StateEngine stateEngine;
    private final EventBus eventBus;
    private final DecayApplicator decayApplicator;
    private final DecaySettings decaySettings;
    private final TSSpearSettings settings;
    private Instant lastRun = Instant.now();

    public DecayScheduler(
        StateEngine stateEngine,
        EventBus eventBus,
        DecayApplicator decayApplicator,
        DecaySettings decaySettings,
        TSSpearSettings settings
    ) {
        this.stateEngine = stateEngine;
        this.eventBus = eventBus;
        this.decayApplicator = decayApplicator;
        this.decaySettings = decaySettings;
        this.settings = settings;
    }

    public void tick() {
        Instant now = Instant.now();
        Duration elapsed = Duration.between(lastRun, now);
        if (elapsed.isZero()) {
            return;
        }
        lastRun = now;

        Collection<PlayerState> players = stateEngine.activePlayers();
        for (PlayerState state : players) {
            applyDecay(state, elapsed);
        }
    }

    private void applyDecay(PlayerState state, Duration elapsed) {
        for (Map.Entry<ConfidenceDimension, DecayCurveConfig> entry : decaySettings.perDimension().entrySet()) {
            ConfidenceDimension dimension = entry.getKey();
            DecayCurveConfig config = entry.getValue();
            DimensionConfidence dim = state.trust().vector().get(dimension);
            double previous = dim.confidence();
            double decayed = decayApplicator.apply(previous, config.curve(), elapsed, config.rate());
            if (Math.abs(decayed - previous) < settings.confidenceEpsilon()) {
                continue;
            }
            dim.setConfidence(decayed);
            dim.setExplanation("Decay applied (" + config.curve().name().toLowerCase() + ")");
            state.bumpVersion();

            ConfidenceReceipt receipt = ConfidenceReceipt.create(
                state.uuid(),
                "DECAY_APPLIED",
                dimension,
                previous,
                decayed,
                dim.explanation(),
                dim.supportingEvidence(),
                state.version(),
                dim.tension(),
                dim.tension(),
                dim.supportingEvidence().size(),
                dim.conflictingEvidence().size(),
                state.movement().lastSample() == null ? null : state.movement().lastSample().position()
            );
            eventBus.publish(new ConfidenceChangedEvent(state.uuid(), dimension, receipt, Instant.now()));
        }
        state.trust().setVector(state.trust().vector());
        state.suspicion().update(state.trust().vector().aggregate());
    }

    public record DecayCurveConfig(DecayCurve curve, double rate) {}
}
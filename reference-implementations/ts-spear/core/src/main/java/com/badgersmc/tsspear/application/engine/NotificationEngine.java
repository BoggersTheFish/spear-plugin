package com.badgersmc.tsspear.application.engine;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.api.confidence.TrendDirection;
import com.badgersmc.tsspear.application.bus.ConfidenceChangedEvent;
import com.badgersmc.tsspear.application.bus.EventBus;
import com.badgersmc.tsspear.application.config.TSSpearSettings;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;
import com.badgersmc.tsspear.domain.service.ExplainabilityService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class NotificationEngine {
    private static final Logger LOGGER = Logger.getLogger(NotificationEngine.class.getName());

    private final TSSpearSettings settings;
    private final StateEngine stateEngine;
    private final ExplainabilityService explainabilityService;
    private final Map<String, Long> rateLimit = new ConcurrentHashMap<>();
    private Consumer<RichAlertPayload> alertSink = payload ->
        LOGGER.info(() -> formatAlert(payload));

    public NotificationEngine(
        TSSpearSettings settings,
        StateEngine stateEngine,
        ExplainabilityService explainabilityService
    ) {
        this.settings = settings;
        this.stateEngine = stateEngine;
        this.explainabilityService = explainabilityService;
    }

    public void setAlertSink(Consumer<RichAlertPayload> alertSink) {
        this.alertSink = alertSink;
    }

    public void start(EventBus eventBus) {
        eventBus.subscribe(ConfidenceChangedEvent.class, this::onConfidenceChanged);
    }

    private void onConfidenceChanged(ConfidenceChangedEvent event) {
        if (event.dimension() == ConfidenceDimension.AGGREGATE) {
            return;
        }
        ConfidenceReceipt receipt = event.receipt();
        if (receipt.newValue() < settings.alertThreshold()) {
            return;
        }
        PlayerState state = stateEngine.get(event.playerId()).orElse(null);
        if (state == null || state.trust().trend() != TrendDirection.RISING) {
            return;
        }
        String key = event.playerId() + ":" + event.dimension();
        long now = System.currentTimeMillis();
        Long last = rateLimit.get(key);
        if (last != null && now - last < 30_000) {
            return;
        }
        rateLimit.put(key, now);

        ExplainabilityService.ExplanationReport report = explainabilityService.explain(state);
        ExplainabilityService.DimensionExplanation dimReport = report.dimensions().stream()
            .filter(d -> d.dimension() == event.dimension())
            .findFirst()
            .orElse(null);

        List<String> supporting = dimReport == null ? List.of() : dimReport.supporting().stream()
            .map(s -> s.kind() + " (" + String.format("%.2f", s.weight()) + ")")
            .limit(3)
            .toList();
        List<String> conflicting = dimReport == null ? List.of() : dimReport.conflicting().stream()
            .map(c -> c.kind() + " (" + String.format("%.2f", c.weight()) + ")")
            .limit(3)
            .toList();

        double net = dimReport == null ? receipt.newValue() : dimReport.netConfidence();
        int evidenceCount = (dimReport == null ? 0 : dimReport.supporting().size())
            + (dimReport == null ? 0 : dimReport.conflicting().size());

        alertSink.accept(new RichAlertPayload(
            event.playerId(),
            state.name(),
            event.dimension(),
            receipt.newValue(),
            receipt.previousValue(),
            receipt.newValue() - receipt.previousValue(),
            state.trust().trend(),
            state.suspicion().currentRisk(),
            state.suspicion().previousRisk(),
            state.suspicion().delta(),
            evidenceCount,
            supporting,
            conflicting,
            net,
            receipt.reason()
        ));
    }

    private static String formatAlert(RichAlertPayload payload) {
        return """
            [TS-Spear Alert] %s — %s confidence=%.2f net=%.2f trend=%s risk=%.2f (Δ=%.2f)
            Supporting: %s
            Conflicting: %s
            Reason: %s
            """.formatted(
            payload.playerName(),
            payload.dimension(),
            payload.confidence(),
            payload.netConfidence(),
            payload.trend(),
            payload.currentRisk(),
            payload.riskDelta(),
            payload.supportingEvidence(),
            payload.conflictingEvidence(),
            payload.reason()
        );
    }

    public record RichAlertPayload(
        UUID playerId,
        String playerName,
        ConfidenceDimension dimension,
        double confidence,
        double previousConfidence,
        double confidenceDelta,
        TrendDirection trend,
        double currentRisk,
        double previousRisk,
        double riskDelta,
        int evidenceCount,
        List<String> supportingEvidence,
        List<String> conflictingEvidence,
        double netConfidence,
        String reason
    ) {}
}
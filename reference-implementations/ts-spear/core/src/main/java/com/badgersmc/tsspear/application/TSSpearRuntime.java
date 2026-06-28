package com.badgersmc.tsspear.application;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.application.bus.ConfidenceChangedEvent;
import com.badgersmc.tsspear.application.bus.EventBus;
import com.badgersmc.tsspear.application.bus.EvidenceProducedEvent;
import com.badgersmc.tsspear.application.config.DecaySettings;
import com.badgersmc.tsspear.application.config.TSSpearSettings;
import com.badgersmc.tsspear.application.engine.CheckEngine;
import com.badgersmc.tsspear.application.engine.EvidenceEngine;
import com.badgersmc.tsspear.application.engine.GraphEngine;
import com.badgersmc.tsspear.application.engine.NotificationEngine;
import com.badgersmc.tsspear.application.engine.PropagationResult;
import com.badgersmc.tsspear.application.engine.ReplayEngine;
import com.badgersmc.tsspear.application.engine.StateEngine;
import com.badgersmc.tsspear.application.engine.StorageEngine;
import com.badgersmc.tsspear.application.metrics.PerformanceMetrics;
import com.badgersmc.tsspear.application.ml.FeatureExtractor;
import com.badgersmc.tsspear.application.ml.MlProviderRegistry;
import com.badgersmc.tsspear.application.ml.NoOpInferenceProvider;
import com.badgersmc.tsspear.application.ml.NoOpPredictionProvider;
import com.badgersmc.tsspear.application.ml.RuleBasedInferenceProvider;
import com.badgersmc.tsspear.application.ml.SequenceBuilder;
import com.badgersmc.tsspear.application.pool.PoolRegistry;
import com.badgersmc.tsspear.application.scheduler.DecayScheduler;
import com.badgersmc.tsspear.application.service.AnnotationService;
import com.badgersmc.tsspear.application.service.ExportService;
import com.badgersmc.tsspear.application.service.FreezeService;
import com.badgersmc.tsspear.application.service.InferenceService;
import com.badgersmc.tsspear.application.service.StaffPreferencesService;
import com.badgersmc.tsspear.application.service.TimelineService;
import com.badgersmc.tsspear.domain.check.Check;
import com.badgersmc.tsspear.domain.check.CheckConfig;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;
import com.badgersmc.tsspear.domain.model.replay.ReplayTriggerReason;
import com.badgersmc.tsspear.domain.port.EvidenceStore;
import com.badgersmc.tsspear.domain.port.InferenceProvider;
import com.badgersmc.tsspear.domain.port.PredictionProvider;
import com.badgersmc.tsspear.domain.port.ReceiptStore;
import com.badgersmc.tsspear.domain.service.ConfidenceResolver;
import com.badgersmc.tsspear.domain.service.DecayApplicator;
import com.badgersmc.tsspear.domain.service.ExplainabilityService;
import com.badgersmc.tsspear.domain.service.TensionCalculator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TSSpearRuntime {
    private final TSSpearSettings settings;
    private final EventBus eventBus;
    private final StateEngine stateEngine;
    private final EvidenceEngine evidenceEngine;
    private final GraphEngine graphEngine;
    private final CheckEngine checkEngine;
    private final NotificationEngine notificationEngine;
    private final ExplainabilityService explainabilityService;
    private final StorageEngine storageEngine;
    private final ReplayEngine replayEngine;
    private final DecayScheduler decayScheduler;
    private final ExportService exportService;
    private final TimelineService timelineService;
    private final FreezeService freezeService;
    private final StaffPreferencesService staffPreferences;
    private final InferenceService inferenceService;
    private final AnnotationService annotationService;
    private final InferenceProvider inferenceProvider;
    private final PredictionProvider predictionProvider;
    private final ReceiptStore receiptStore;
    private final EvidenceStore evidenceStore;
    private final PerformanceMetrics performanceMetrics;

    public TSSpearRuntime(TSSpearSettings settings, DecaySettings decaySettings, RuntimeDependencies deps, List<Check> checks) {
        this.settings = settings;
        this.eventBus = new EventBus();
        this.receiptStore = deps.receiptStore();
        this.evidenceStore = deps.evidenceStore();
        this.stateEngine = new StateEngine(deps.playerStateRepository());
        this.evidenceEngine = new EvidenceEngine(evidenceStore, settings.evidenceDedupeWindowMs());
        ConfidenceResolver resolver = new ConfidenceResolver(settings.tensionSensitivity());
        TensionCalculator tensionCalculator = new TensionCalculator(settings.contradictionMultiplier());
        this.graphEngine = new GraphEngine(resolver, tensionCalculator, settings);
        this.explainabilityService = new ExplainabilityService(resolver, 5);
        this.performanceMetrics = new PerformanceMetrics();
        CheckConfig checkConfig = buildCheckConfig(settings);
        this.checkEngine = new CheckEngine(stateEngine, eventBus, checkConfig, settings.workerThreads(), performanceMetrics);
        for (Check check : checks) {
            checkEngine.register(check);
        }
        this.notificationEngine = new NotificationEngine(settings, stateEngine, explainabilityService);
        this.storageEngine = new StorageEngine(
            receiptStore,
            evidenceStore,
            settings.storageBatchSize(),
            settings.storageFlushIntervalMs(),
            performanceMetrics
        );
        this.replayEngine = new ReplayEngine(
            deps.replayStore(),
            settings.replayRingBufferSeconds(),
            settings.replayTicksPerSecond(),
            settings.continuousCaptureSeconds()
        );
        this.freezeService = new FreezeService(replayEngine);
        this.staffPreferences = new StaffPreferencesService();
        this.decayScheduler = new DecayScheduler(
            stateEngine,
            eventBus,
            new DecayApplicator(),
            decaySettings,
            settings
        );

        FeatureExtractor featureExtractor = new FeatureExtractor();
        SequenceBuilder sequenceBuilder = new SequenceBuilder();
        this.annotationService = new AnnotationService(deps.annotationStore());

        MlProviderRegistry mlRegistry = new MlProviderRegistry()
            .register(new NoOpInferenceProvider())
            .register(new RuleBasedInferenceProvider(
                settings.mlRuleBasedCheatingThreshold(),
                settings.mlRuleBasedSuspiciousThreshold()
            ));
        mlRegistry.setActiveProvider(settings.mlInferenceProvider());

        this.inferenceService = new InferenceService(mlRegistry, featureExtractor);
        this.inferenceProvider = mlRegistry.active().orElseGet(NoOpInferenceProvider::new);
        this.predictionProvider = new NoOpPredictionProvider();
        this.exportService = new ExportService(
            receiptStore,
            featureExtractor,
            sequenceBuilder,
            annotationService,
            settings.mlExportWindowSeconds()
        );
        this.timelineService = new TimelineService(receiptStore);
    }

    public void start() {
        PoolRegistry.install(settings);
        eventBus.subscribe(EvidenceProducedEvent.class, this::onEvidenceProduced);
        eventBus.subscribe(ConfidenceChangedEvent.class, this::onConfidenceChanged);
        checkEngine.start();
        replayEngine.start(eventBus);
        notificationEngine.start(eventBus);
    }

    public void shutdown() {
        checkEngine.shutdown();
        stateEngine.persistAll();
        storageEngine.flushAndShutdown(5000);
    }

    private void onEvidenceProduced(EvidenceProducedEvent event) {
        PlayerState state = stateEngine.getOrCreate(event.playerId(), event.playerId().toString());
        List<Evidence> accepted = evidenceEngine.ingest(event.evidence());
        if (accepted.isEmpty()) {
            return;
        }
        for (Evidence evidence : accepted) {
            if (evidence.weight() >= settings.evidencePersistThreshold()) {
                storageEngine.enqueueEvidence(evidence);
            }
        }
        PropagationResult result = graphEngine.propagate(state, accepted, performanceMetrics);
        for (ConfidenceReceipt receipt : result.receipts()) {
            storageEngine.enqueueReceipt(receipt);
            eventBus.publish(new ConfidenceChangedEvent(
                event.playerId(),
                receipt.dimension(),
                receipt,
                Instant.now()
            ));
        }
    }

    private void onConfidenceChanged(ConfidenceChangedEvent event) {
        storageEngine.enqueueReceipt(event.receipt());
        if (event.dimension() == ConfidenceDimension.AGGREGATE) {
            return;
        }
        ConfidenceReceipt receipt = event.receipt();
        if (receipt.newValue() < settings.alertThreshold()) {
            return;
        }
        stateEngine.get(event.playerId()).ifPresent(state -> {
            if (!freezeService.isFrozen(event.playerId()) && !replayEngine.isContinuousCapture(event.playerId())) {
                replayEngine.enableContinuousCapture(event.playerId());
                replayEngine.snapshot(event.playerId(), state.name(), ReplayTriggerReason.ALERT);
            }
        });
    }

    public EventBus eventBus() { return eventBus; }
    public StateEngine stateEngine() { return stateEngine; }
    public CheckEngine checkEngine() { return checkEngine; }
    public ExplainabilityService explainabilityService() { return explainabilityService; }
    public NotificationEngine notificationEngine() { return notificationEngine; }
    public ReplayEngine replayEngine() { return replayEngine; }
    public DecayScheduler decayScheduler() { return decayScheduler; }
    public ExportService exportService() { return exportService; }
    public TimelineService timelineService() { return timelineService; }
    public FreezeService freezeService() { return freezeService; }
    public StaffPreferencesService staffPreferences() { return staffPreferences; }
    public InferenceService inferenceService() { return inferenceService; }
    public AnnotationService annotationService() { return annotationService; }
    public InferenceProvider inferenceProvider() { return inferenceProvider; }
    public PredictionProvider predictionProvider() { return predictionProvider; }
    public ReceiptStore receiptStore() { return receiptStore; }
    public EvidenceStore evidenceStore() { return evidenceStore; }
    public Optional<PlayerState> playerState(UUID id) { return stateEngine.get(id); }
    public TSSpearSettings settings() { return settings; }
    public PerformanceMetrics performanceMetrics() { return performanceMetrics; }
    public StorageEngine storageEngine() { return storageEngine; }

    private static CheckConfig buildCheckConfig(TSSpearSettings settings) {
        CheckConfig config = new CheckConfig();
        config.setEnabled("impossible-movement", true);
        config.setEnabled("lag-spike", true);
        config.setEnabled("fly-check", true);
        config.setEnabled("speed-check", true);
        config.setEnabled("reach-check", true);
        config.setEnabled("killaura-check", true);
        config.setEnabled("velocity-check", true);
        config.setEnabled("bad-packet-check", true);
        config.setEnabled("blink-check", true);
        config.setDouble("max-acceleration", settings.maxAcceleration());
        config.setDouble("max-speed", settings.maxSpeed());
        config.setInt("lag-threshold-ms", settings.lagThresholdMs());
        config.setInt("max-air-ticks", settings.maxAirTicks());
        config.setDouble("max-reach", settings.maxReach());
        config.setInt("killaura-window-ticks", settings.killAuraWindowTicks());
        config.setInt("killaura-victim-threshold", settings.killAuraVictimThreshold());
        config.setDouble("max-knockback-damage-ratio", settings.maxKnockbackDamageRatio());
        config.setInt("blink-min-interval-ms", settings.blinkMinIntervalMs());
        config.setDouble("bad-packet-position-threshold", settings.badPacketPositionThreshold());
        return config;
    }
}
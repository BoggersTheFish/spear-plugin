package com.badgersmc.tsspear.application.service;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.api.graph.GraphKind;
import com.badgersmc.tsspear.application.ml.ExportTarget;
import com.badgersmc.tsspear.application.ml.FeatureExtractor;
import com.badgersmc.tsspear.application.ml.FeatureVector;
import com.badgersmc.tsspear.application.ml.JsonEvidenceExporter;
import com.badgersmc.tsspear.application.ml.JsonLinesEvidenceExporter;
import com.badgersmc.tsspear.application.ml.MlExportDocument;
import com.badgersmc.tsspear.application.ml.SequenceBuilder;
import com.badgersmc.tsspear.application.ml.TimeWindow;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.port.EvidenceExporter;
import com.badgersmc.tsspear.domain.port.ReceiptStore;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ExportService {
    private final ReceiptStore receiptStore;
    private final FeatureExtractor featureExtractor;
    private final SequenceBuilder sequenceBuilder;
    private final AnnotationService annotationService;
    private final int exportWindowSeconds;
    private final EvidenceExporter jsonExporter = new JsonEvidenceExporter();
    private final EvidenceExporter jsonlExporter = new JsonLinesEvidenceExporter();

    public ExportService(
        ReceiptStore receiptStore,
        FeatureExtractor featureExtractor,
        SequenceBuilder sequenceBuilder,
        AnnotationService annotationService,
        int exportWindowSeconds
    ) {
        this.receiptStore = receiptStore;
        this.featureExtractor = featureExtractor;
        this.sequenceBuilder = sequenceBuilder;
        this.annotationService = annotationService;
        this.exportWindowSeconds = exportWindowSeconds;
    }

    public PathResult exportPlayer(PlayerState state, ExportTarget target, int receiptLimit) {
        TimeWindow window = TimeWindow.lastSeconds(exportWindowSeconds);
        FeatureVector featureVector = featureExtractor.extract(state, window);

        Map<String, Double> confidence = new HashMap<>();
        for (ConfidenceDimension dim : ConfidenceDimension.values()) {
            if (dim == ConfidenceDimension.AGGREGATE) {
                continue;
            }
            confidence.put(dim.name().toLowerCase(), state.trust().vector().get(dim).confidence());
        }
        confidence.put("aggregate", state.trust().vector().aggregate());

        Map<String, Object> graph = serializeGraph(state);
        float[][] movement = state.movement().lastSample() == null
            ? new float[0][0]
            : sequenceBuilder.buildMovementSequence(List.of(state.movement().lastSample()));
        float[][] combat = sequenceBuilder.buildCombatSequence(state.combat().recentHits());
        float[][] packets = sequenceBuilder.buildPacketSequence(state.networking().recentPackets());

        Map<String, Object> label = annotationService.labelForExport(state.uuid()).orElse(Map.of());

        MlExportDocument document = new MlExportDocument(
            1,
            UUID.randomUUID(),
            Instant.now(),
            state.uuid(),
            window,
            confidence,
            graph,
            movement,
            combat,
            packets,
            featureVector.values(),
            List.of(featureVector.names()),
            receiptStore.findByPlayer(state.uuid(), receiptLimit),
            label
        );

        exporterFor(target.format()).export(document, target);
        return new PathResult(document.exportId(), target.filePath());
    }

    private EvidenceExporter exporterFor(ExportTarget.ExportFormat format) {
        return format == ExportTarget.ExportFormat.JSONL ? jsonlExporter : jsonExporter;
    }

    private Map<String, Object> serializeGraph(PlayerState state) {
        Map<String, Object> graphs = new HashMap<>();
        for (GraphKind kind : GraphKind.values()) {
            List<Map<String, Object>> nodes = state.graph(kind).nodes().stream().map(n -> Map.<String, Object>of(
                "id", n.nodeId(),
                "activation", n.activation(),
                "confidence", n.confidence(),
                "reason", n.reason()
            )).toList();
            List<Map<String, Object>> edges = state.graph(kind).edges().stream().map(e -> Map.<String, Object>of(
                "from", e.fromNodeId(),
                "to", e.toNodeId(),
                "kind", e.kind().name(),
                "strength", e.strength()
            )).toList();
            graphs.put(kind.name().toLowerCase(), Map.of("nodes", nodes, "edges", edges));
        }
        return graphs;
    }

    public record PathResult(UUID exportId, java.nio.file.Path path) {}
}
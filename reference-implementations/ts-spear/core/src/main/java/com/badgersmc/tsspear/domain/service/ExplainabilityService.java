package com.badgersmc.tsspear.domain.service;

import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.domain.model.confidence.DimensionConfidence;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.player.PlayerState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ExplainabilityService {
    private final ConfidenceResolver resolver;
    private final int maxEvidencePerSide;

    public ExplainabilityService(ConfidenceResolver resolver, int maxEvidencePerSide) {
        this.resolver = resolver;
        this.maxEvidencePerSide = maxEvidencePerSide;
    }

    public ExplanationReport explain(PlayerState state) {
        Map<UUID, Evidence> evidenceById = state.recentEvidence().stream()
            .collect(Collectors.toMap(e -> e.id().value(), Function.identity(), (a, b) -> a));

        List<DimensionExplanation> dimensions = new ArrayList<>();
        for (ConfidenceDimension dim : ConfidenceDimension.values()) {
            if (dim == ConfidenceDimension.AGGREGATE) {
                continue;
            }
            DimensionConfidence dc = state.trust().vector().get(dim);
            List<EvidenceSummary> supporting = rankEvidence(dc.supportingEvidence(), evidenceById, true);
            List<EvidenceSummary> conflicting = rankEvidence(dc.conflictingEvidence(), evidenceById, false);
            double supportWeight = supporting.stream().mapToDouble(EvidenceSummary::weight).sum();
            double conflictWeight = conflicting.stream().mapToDouble(EvidenceSummary::weight).sum();
            double net = resolver.netConfidence(dc.confidence(), supportWeight, conflictWeight);
            dimensions.add(new DimensionExplanation(
                dim, dc.confidence(), net, dc.tension(), supporting, conflicting, dc.explanation()
            ));
        }
        return new ExplanationReport(state.uuid(), state.name(), dimensions, state.trust(), state.suspicion());
    }

    private List<EvidenceSummary> rankEvidence(
        List<com.badgersmc.tsspear.domain.model.ids.EvidenceId> ids,
        Map<UUID, Evidence> evidenceById,
        boolean supporting
    ) {
        return ids.stream()
            .map(id -> Optional.ofNullable(evidenceById.get(id.value())))
            .flatMap(Optional::stream)
            .sorted(Comparator.comparingDouble(Evidence::weight).reversed())
            .limit(maxEvidencePerSide)
            .map(e -> new EvidenceSummary(
                e.kind().name(),
                e.reason(),
                e.weight(),
                e.timestamp(),
                supporting
            ))
            .toList();
    }

    public record EvidenceSummary(String kind, String reason, double weight, java.time.Instant at, boolean supporting) {}

    public record DimensionExplanation(
        ConfidenceDimension dimension,
        double confidence,
        double netConfidence,
        double tension,
        List<EvidenceSummary> supporting,
        List<EvidenceSummary> conflicting,
        String explanation
    ) {}

    public record ExplanationReport(
        UUID playerId,
        String playerName,
        List<DimensionExplanation> dimensions,
        com.badgersmc.tsspear.domain.model.player.TrustState trust,
        com.badgersmc.tsspear.domain.model.player.SuspicionState suspicion
    ) {}
}
package com.badgersmc.tsspear.domain.model.player;

import com.badgersmc.tsspear.api.graph.GraphKind;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.graph.PlayerGraph;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PlayerState {
    private final UUID uuid;
    private final String name;
    private long version;
    private final MovementState movement = new MovementState();
    private final CombatState combat = new CombatState();
    private final NetworkingState networking = new NetworkingState();
    private final LatencyState latency = new LatencyState();
    private final TrustState trust = new TrustState();
    private final SuspicionState suspicion = new SuspicionState();
    private final Map<GraphKind, PlayerGraph> graphs = new EnumMap<>(GraphKind.class);
    private final List<Evidence> recentEvidence = new ArrayList<>();
    private Instant lastMutation = Instant.now();

    public PlayerState(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        for (GraphKind kind : GraphKind.values()) {
            graphs.put(kind, new PlayerGraph(kind));
        }
        trust.setVector(com.badgersmc.tsspear.domain.model.confidence.ConfidenceVector.empty());
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public long version() {
        return version;
    }

    public void bumpVersion() {
        version++;
        lastMutation = Instant.now();
    }

    public MovementState movement() {
        return movement;
    }

    public CombatState combat() {
        return combat;
    }

    public NetworkingState networking() {
        return networking;
    }

    public LatencyState latency() {
        return latency;
    }

    public TrustState trust() {
        return trust;
    }

    public SuspicionState suspicion() {
        return suspicion;
    }

    public PlayerGraph graph(GraphKind kind) {
        return graphs.get(kind);
    }

    public List<Evidence> recentEvidence() {
        return List.copyOf(recentEvidence);
    }

    public void addEvidence(Evidence evidence) {
        recentEvidence.add(evidence);
        if (recentEvidence.size() > 200) {
            recentEvidence.remove(0);
        }
        bumpVersion();
    }

    public Instant lastMutation() {
        return lastMutation;
    }

    public PlayerState snapshot() {
        PlayerState copy = new PlayerState(uuid, name);
        copy.version = version;
        if (movement.lastSample() != null) {
            copy.movement.recordSample(movement.lastSample());
        }
        copy.trust.setVector(trust.vector().snapshot());
        copy.suspicion.update(suspicion.currentRisk());
        copy.recentEvidence.addAll(recentEvidence);
        copy.lastMutation = lastMutation;
        return copy;
    }
}
package com.badgersmc.tsspear.application.engine;

import com.badgersmc.tsspear.application.bus.EventBus;
import com.badgersmc.tsspear.application.bus.PlayerSampleEvent;
import com.badgersmc.tsspear.domain.model.replay.ReplayFrame;
import com.badgersmc.tsspear.domain.model.replay.ReplayFrameType;
import com.badgersmc.tsspear.domain.model.replay.ReplaySequence;
import com.badgersmc.tsspear.domain.model.replay.ReplayTriggerReason;
import com.badgersmc.tsspear.domain.model.sample.MovementSample;
import com.badgersmc.tsspear.domain.port.ReplayStore;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ReplayEngine {
    private final int maxFrames;
    private final int continuousMaxFrames;
    private final ReplayStore replayStore;
    private final Map<UUID, Deque<ReplayFrame>> buffers = new ConcurrentHashMap<>();
    private final Set<UUID> continuousCapture = ConcurrentHashMap.newKeySet();

    public ReplayEngine(
        ReplayStore replayStore,
        int ringBufferSeconds,
        int ticksPerSecond,
        int continuousCaptureSeconds
    ) {
        this.replayStore = replayStore;
        this.maxFrames = ringBufferSeconds * ticksPerSecond;
        this.continuousMaxFrames = continuousCaptureSeconds * ticksPerSecond;
    }

    public void start(EventBus eventBus) {
        eventBus.subscribe(PlayerSampleEvent.class, this::onSample);
    }

    private void onSample(PlayerSampleEvent event) {
        MovementSample sample = event.sample();
        ReplayFrame frame = new ReplayFrame(
            sample.tick(),
            sample.timestamp(),
            ReplayFrameType.MOVEMENT,
            sample.position(),
            sample.velocity(),
            sample.onGround(),
            sample.pingMs(),
            Map.of("gameMode", sample.gameMode())
        );
        buffers.computeIfAbsent(sample.playerId(), id -> new ArrayDeque<>()).addLast(frame);
        trim(sample.playerId());
    }

    private void trim(UUID playerId) {
        int limit = bufferLimit(playerId);
        Deque<ReplayFrame> deque = buffers.get(playerId);
        while (deque != null && deque.size() > limit) {
            deque.removeFirst();
        }
    }

    private int bufferLimit(UUID playerId) {
        return continuousCapture.contains(playerId) ? continuousMaxFrames : maxFrames;
    }

    public void enableContinuousCapture(UUID playerId) {
        continuousCapture.add(playerId);
    }

    public void disableContinuousCapture(UUID playerId) {
        continuousCapture.remove(playerId);
        trim(playerId);
    }

    public boolean isContinuousCapture(UUID playerId) {
        return continuousCapture.contains(playerId);
    }

    public List<ReplayFrame> liveBuffer(UUID playerId) {
        Deque<ReplayFrame> deque = buffers.get(playerId);
        return deque == null ? List.of() : List.copyOf(deque);
    }

    public Optional<ReplaySequence> snapshot(UUID playerId, String playerName, ReplayTriggerReason reason) {
        List<ReplayFrame> frames = liveBuffer(playerId);
        if (frames.isEmpty()) {
            return Optional.empty();
        }
        ReplayFrame first = frames.get(0);
        ReplayFrame last = frames.get(frames.size() - 1);
        ReplaySequence sequence = new ReplaySequence(
            UUID.randomUUID(),
            playerId,
            playerName,
            first.tick(),
            last.tick(),
            first.timestamp(),
            reason,
            frames
        );
        replayStore.store(sequence);
        return Optional.of(sequence);
    }

    public Optional<ReplaySequence> resolveForViewer(UUID playerId, String playerName) {
        return snapshot(playerId, playerName, ReplayTriggerReason.STAFF_REQUEST)
            .or(() -> latestStored(playerId));
    }

    public Optional<ReplaySequence> latestStored(UUID playerId) {
        return replayStore.findLatestForPlayer(playerId);
    }

    public Optional<ReplaySequence> findById(UUID sequenceId) {
        return replayStore.findById(sequenceId);
    }

    public Optional<ReplaySequence> resolveSequence(UUID playerId, String playerName, UUID sequenceId) {
        if (sequenceId != null) {
            Optional<ReplaySequence> stored = findById(sequenceId);
            if (stored.isPresent()) {
                return stored;
            }
        }
        List<ReplayFrame> live = liveBuffer(playerId);
        if (live.isEmpty()) {
            return latestStored(playerId);
        }
        ReplayFrame first = live.get(0);
        ReplayFrame last = live.get(live.size() - 1);
        return Optional.of(new ReplaySequence(
            sequenceId == null ? UUID.randomUUID() : sequenceId,
            playerId,
            playerName,
            first.tick(),
            last.tick(),
            first.timestamp(),
            ReplayTriggerReason.RING_BUFFER,
            new ArrayList<>(live)
        ));
    }

    public void clear(UUID playerId) {
        buffers.remove(playerId);
        continuousCapture.remove(playerId);
    }
}
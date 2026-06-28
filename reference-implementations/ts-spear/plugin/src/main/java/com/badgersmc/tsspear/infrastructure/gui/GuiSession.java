package com.badgersmc.tsspear.infrastructure.gui;

import com.badgersmc.tsspear.api.graph.GraphKind;

import java.util.UUID;

public record GuiSession(
    GuiType type,
    UUID targetId,
    String targetName,
    int page,
    GraphKind graphKind,
    int replayFrameIndex,
    UUID replaySequenceId
) {
    public static GuiSession inspect(UUID targetId, String targetName) {
        return new GuiSession(GuiType.INSPECT, targetId, targetName, 0, GraphKind.TENSION, 0, null);
    }

    public static GuiSession evidence(UUID targetId, String targetName, int page) {
        return new GuiSession(GuiType.EVIDENCE, targetId, targetName, page, GraphKind.EVIDENCE, 0, null);
    }

    public static GuiSession timeline(UUID targetId, String targetName) {
        return new GuiSession(GuiType.TIMELINE, targetId, targetName, 0, GraphKind.HISTORY, 0, null);
    }

    public static GuiSession graph(UUID targetId, String targetName, GraphKind kind) {
        return new GuiSession(GuiType.GRAPH, targetId, targetName, 0, kind, 0, null);
    }

    public static GuiSession replay(UUID targetId, String targetName, UUID sequenceId, int frameIndex) {
        return new GuiSession(GuiType.REPLAY, targetId, targetName, 0, GraphKind.HISTORY, frameIndex, sequenceId);
    }

    public static GuiSession checks() {
        return new GuiSession(GuiType.CHECKS, null, "", 0, GraphKind.TENSION, 0, null);
    }
}
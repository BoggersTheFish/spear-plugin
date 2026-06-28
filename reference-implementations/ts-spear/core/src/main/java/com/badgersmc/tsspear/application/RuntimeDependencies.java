package com.badgersmc.tsspear.application;

import com.badgersmc.tsspear.domain.port.AnnotationStore;
import com.badgersmc.tsspear.domain.port.EvidenceStore;
import com.badgersmc.tsspear.domain.port.PlayerStateRepository;
import com.badgersmc.tsspear.domain.port.ReceiptStore;
import com.badgersmc.tsspear.domain.port.ReplayStore;

public record RuntimeDependencies(
    PlayerStateRepository playerStateRepository,
    ReceiptStore receiptStore,
    EvidenceStore evidenceStore,
    ReplayStore replayStore,
    AnnotationStore annotationStore
) {}
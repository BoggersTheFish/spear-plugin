package com.badgersmc.tsspear.domain.model.ids;

import java.util.Objects;
import java.util.UUID;

public record EvidenceId(UUID value) {
    public EvidenceId {
        Objects.requireNonNull(value, "value");
    }

    public static EvidenceId generate() {
        return new EvidenceId(UUID.randomUUID());
    }

    public static EvidenceId of(UUID value) {
        return new EvidenceId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
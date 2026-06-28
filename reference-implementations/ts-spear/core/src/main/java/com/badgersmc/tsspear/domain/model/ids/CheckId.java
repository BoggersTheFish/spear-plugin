package com.badgersmc.tsspear.domain.model.ids;

import java.util.Objects;

public record CheckId(String value) {
    public CheckId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("CheckId must not be blank");
        }
    }

    public static CheckId of(String value) {
        return new CheckId(value);
    }
}
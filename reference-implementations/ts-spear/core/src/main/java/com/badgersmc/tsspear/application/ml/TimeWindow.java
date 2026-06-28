package com.badgersmc.tsspear.application.ml;

import java.time.Instant;

public record TimeWindow(Instant start, Instant end, long tickStart, long tickEnd) {
    public static TimeWindow lastSeconds(int seconds) {
        Instant end = Instant.now();
        return new TimeWindow(end.minusSeconds(seconds), end, 0L, 0L);
    }
}
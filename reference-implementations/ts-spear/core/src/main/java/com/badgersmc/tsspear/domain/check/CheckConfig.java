package com.badgersmc.tsspear.domain.check;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CheckConfig {
    private final Map<String, Boolean> enabled;
    private final Map<String, Double> doubles;
    private final Map<String, Integer> integers;

    public CheckConfig() {
        this.enabled = new HashMap<>();
        this.doubles = new HashMap<>();
        this.integers = new HashMap<>();
    }

    public CheckConfig(Map<String, Boolean> enabled, Map<String, Double> doubles, Map<String, Integer> integers) {
        this.enabled = new HashMap<>(enabled);
        this.doubles = new HashMap<>(doubles);
        this.integers = new HashMap<>(integers);
    }

    public boolean isEnabled(String checkId) {
        return enabled.getOrDefault(checkId, true);
    }

    public double getDouble(String key, double defaultValue) {
        return doubles.getOrDefault(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return integers.getOrDefault(key, defaultValue);
    }

    public void setEnabled(String checkId, boolean value) {
        enabled.put(checkId, value);
    }

    public void setDouble(String key, double value) {
        doubles.put(key, value);
    }

    public void setInt(String key, int value) {
        integers.put(key, value);
    }

    public Map<String, Boolean> enabledChecks() {
        return Collections.unmodifiableMap(enabled);
    }
}
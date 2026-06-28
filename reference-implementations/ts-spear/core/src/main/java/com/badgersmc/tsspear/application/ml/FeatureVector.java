package com.badgersmc.tsspear.application.ml;

public record FeatureVector(float[] values, String[] names) {
    public int size() {
        return values.length;
    }
}
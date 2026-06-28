package com.badgersmc.tsspear.api.ml;

public enum AnnotationVerdict {
    LEGIT,
    SUSPICIOUS,
    CHEATING,
    INCONCLUSIVE;

    public static AnnotationVerdict parse(String value) {
        return AnnotationVerdict.valueOf(value.toUpperCase());
    }
}
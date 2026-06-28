package com.badgersmc.tsspear.application.ml;

import java.nio.file.Path;

public record ExportTarget(Path filePath, ExportFormat format) {
    public enum ExportFormat {
        JSON,
        JSONL
    }
}
package com.badgersmc.tsspear.application.ml;

import com.badgersmc.tsspear.domain.port.EvidenceExporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public final class JsonLinesEvidenceExporter implements EvidenceExporter {
    private final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void export(MlExportDocument document, ExportTarget target) {
        try {
            Files.createDirectories(target.filePath().getParent());
            String line = mapper.writeValueAsString(document);
            Files.writeString(
                target.filePath(),
                line + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (Exception ex) {
            throw new IllegalStateException("JSONL export failed: " + target.filePath(), ex);
        }
    }
}
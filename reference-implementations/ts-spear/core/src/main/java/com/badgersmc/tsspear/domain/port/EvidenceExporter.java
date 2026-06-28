package com.badgersmc.tsspear.domain.port;

import com.badgersmc.tsspear.application.ml.ExportTarget;
import com.badgersmc.tsspear.application.ml.MlExportDocument;

public interface EvidenceExporter {
    void export(MlExportDocument document, ExportTarget target);
}
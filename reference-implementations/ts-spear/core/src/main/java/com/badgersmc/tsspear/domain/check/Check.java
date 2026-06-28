package com.badgersmc.tsspear.domain.check;

import com.badgersmc.tsspear.api.check.CheckCategory;
import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.ids.CheckId;

import java.util.List;

public interface Check {
    CheckId id();

    CheckCategory category();

    ConfidenceDimension dimension();

    boolean isEnabled(CheckConfig config);

    List<Evidence> evaluate(CheckContext context);
}
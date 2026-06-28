package com.badgersmc.tsspear.domain.check;

import com.badgersmc.tsspear.api.check.CheckCategory;
import com.badgersmc.tsspear.api.confidence.ConfidenceDimension;
import com.badgersmc.tsspear.domain.model.ids.CheckId;
import com.badgersmc.tsspear.domain.model.sample.CombatSample;

public abstract class AbstractCombatCheck extends AbstractCheck {
    protected AbstractCombatCheck(CheckId id) {
        super(id, CheckCategory.COMBAT, ConfidenceDimension.COMBAT);
    }

    protected CombatSample combatSample(CheckContext ctx) {
        return ctx.combatSample();
    }
}
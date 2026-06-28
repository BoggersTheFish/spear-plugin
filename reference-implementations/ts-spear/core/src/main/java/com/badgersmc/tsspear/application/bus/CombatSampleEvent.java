package com.badgersmc.tsspear.application.bus;

import com.badgersmc.tsspear.domain.model.sample.CombatSample;

import java.time.Instant;

public record CombatSampleEvent(CombatSample sample, Instant timestamp) implements TSEvent {}
package com.badgersmc.tsspear.application.bus;

import com.badgersmc.tsspear.domain.model.sample.MovementSample;

import java.time.Instant;

public record PlayerSampleEvent(MovementSample sample, Instant timestamp) implements TSEvent {}
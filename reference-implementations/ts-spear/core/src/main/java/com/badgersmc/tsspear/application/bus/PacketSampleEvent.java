package com.badgersmc.tsspear.application.bus;

import com.badgersmc.tsspear.domain.model.sample.PacketSample;

import java.time.Instant;

public record PacketSampleEvent(PacketSample sample, Instant timestamp) implements TSEvent {}
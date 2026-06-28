package com.badgersmc.tsspear.application.engine;

import com.badgersmc.tsspear.domain.model.receipt.ConfidenceReceipt;

import java.util.List;

public record PropagationResult(List<ConfidenceReceipt> receipts) {}
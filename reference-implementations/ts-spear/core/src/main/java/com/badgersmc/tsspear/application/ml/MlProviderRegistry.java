package com.badgersmc.tsspear.application.ml;

import com.badgersmc.tsspear.domain.port.InferenceProvider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class MlProviderRegistry {
    private final Map<String, InferenceProvider> providers = new LinkedHashMap<>();
    private String activeProviderId;

    public MlProviderRegistry register(InferenceProvider provider) {
        providers.put(provider.providerId(), provider);
        if (activeProviderId == null) {
            activeProviderId = provider.providerId();
        }
        return this;
    }

    public void setActiveProvider(String providerId) {
        if (!providers.containsKey(providerId)) {
            throw new IllegalArgumentException("Unknown inference provider: " + providerId);
        }
        activeProviderId = providerId;
    }

    public Optional<InferenceProvider> active() {
        if (activeProviderId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(providers.get(activeProviderId));
    }

    public Map<String, InferenceProvider> all() {
        return Map.copyOf(providers);
    }
}
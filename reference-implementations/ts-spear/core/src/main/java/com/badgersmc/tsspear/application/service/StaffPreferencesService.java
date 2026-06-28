package com.badgersmc.tsspear.application.service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StaffPreferencesService {
    private final Set<UUID> alertsDisabled = ConcurrentHashMap.newKeySet();

    public boolean alertsEnabled(UUID staffId) {
        return !alertsDisabled.contains(staffId);
    }

    public void setAlertsEnabled(UUID staffId, boolean enabled) {
        if (enabled) {
            alertsDisabled.remove(staffId);
        } else {
            alertsDisabled.add(staffId);
        }
    }
}
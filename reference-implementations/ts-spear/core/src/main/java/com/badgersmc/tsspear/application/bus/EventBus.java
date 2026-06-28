package com.badgersmc.tsspear.application.bus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {
    private final Map<Class<?>, List<EventHandler<?>>> handlers = new ConcurrentHashMap<>();

    public <T extends TSEvent> void subscribe(Class<T> type, EventHandler<T> handler) {
        handlers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @SuppressWarnings("unchecked")
    public <T extends TSEvent> void publish(T event) {
        List<EventHandler<?>> typeHandlers = handlers.get(event.getClass());
        if (typeHandlers == null) {
            return;
        }
        for (EventHandler<?> handler : typeHandlers) {
            ((EventHandler<T>) handler).handle(event);
        }
    }
}
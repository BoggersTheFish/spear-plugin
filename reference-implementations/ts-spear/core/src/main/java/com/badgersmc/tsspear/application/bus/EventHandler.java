package com.badgersmc.tsspear.application.bus;

@FunctionalInterface
public interface EventHandler<T extends TSEvent> {
    void handle(T event);
}
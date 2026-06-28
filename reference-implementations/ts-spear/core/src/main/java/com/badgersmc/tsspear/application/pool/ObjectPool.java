package com.badgersmc.tsspear.application.pool;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ObjectPool<T> {
    private final Deque<T> pool = new ArrayDeque<>();
    private final int maxSize;
    private final Supplier<T> factory;
    private final Consumer<T> reset;

    public ObjectPool(int maxSize, Supplier<T> factory, Consumer<T> reset) {
        this.maxSize = Math.max(1, maxSize);
        this.factory = factory;
        this.reset = reset;
    }

    public T acquire() {
        T item = pool.pollFirst();
        return item == null ? factory.get() : item;
    }

    public void release(T item) {
        if (item == null) {
            return;
        }
        reset.accept(item);
        if (pool.size() < maxSize) {
            pool.addLast(item);
        }
    }

    public int size() {
        return pool.size();
    }
}
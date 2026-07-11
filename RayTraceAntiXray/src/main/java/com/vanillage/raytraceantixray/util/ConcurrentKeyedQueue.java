package com.vanillage.raytraceantixray.util;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * Concurrent FIFO queues partitioned by key. Queue creation, mutation, and removal are performed inside
 * {@link ConcurrentHashMap#compute} operations so an empty queue cannot be removed while another thread appends to it.
 */
public final class ConcurrentKeyedQueue<K, V> {
    private final ConcurrentMap<K, ConcurrentLinkedQueue<V>> queues = new ConcurrentHashMap<>();

    public void add(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        queues.compute(key, (ignored, queue) -> {
            ConcurrentLinkedQueue<V> target = queue != null ? queue : new ConcurrentLinkedQueue<>();
            target.add(value);
            return target;
        });
    }

    public V poll(K key) {
        Objects.requireNonNull(key, "key");
        AtomicReference<V> result = new AtomicReference<>();
        queues.computeIfPresent(key, (ignored, queue) -> {
            result.set(queue.poll());
            return queue.isEmpty() ? null : queue;
        });
        return result.get();
    }

    public void removeValuesIf(Predicate<? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        for (K key : queues.keySet()) {
            queues.computeIfPresent(key, (ignored, queue) -> {
                queue.removeIf(predicate);
                return queue.isEmpty() ? null : queue;
            });
        }
    }

    public void clear() {
        queues.clear();
    }

    public int keyCount() {
        return queues.size();
    }
}

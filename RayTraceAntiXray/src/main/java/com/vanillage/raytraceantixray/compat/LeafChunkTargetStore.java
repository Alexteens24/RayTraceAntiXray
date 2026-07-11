package com.vanillage.raytraceantixray.compat;

import java.util.Objects;
import java.util.function.Predicate;

import com.vanillage.raytraceantixray.util.ConcurrentKeyedQueue;

/** FIFO target queues isolated by dimension and chunk key. */
final class LeafChunkTargetStore<T> {
    private final ConcurrentKeyedQueue<TargetKey, T> targets = new ConcurrentKeyedQueue<>();

    void add(Object dimension, long chunkKey, T target) {
        targets.add(new TargetKey(dimension, chunkKey), target);
    }

    T poll(Object dimension, long chunkKey) {
        return targets.poll(new TargetKey(dimension, chunkKey));
    }

    void removeIf(Predicate<? super T> predicate) {
        targets.removeValuesIf(predicate);
    }

    void clear() {
        targets.clear();
    }

    int keyCount() {
        return targets.keyCount();
    }

    private record TargetKey(Object dimension, long chunkKey) {
        private TargetKey {
            Objects.requireNonNull(dimension, "dimension");
        }
    }
}

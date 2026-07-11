package com.vanillage.raytraceantixray;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import com.vanillage.raytraceantixray.util.ConcurrentKeyedQueue;

/** Pending obfuscated chunk payloads for currently registered players. */
final class PendingChunkBlocksStore<T> {
    private final ConcurrentMap<UUID, ConcurrentKeyedQueue<Long, T>> players = new ConcurrentHashMap<>();

    void registerPlayer(UUID playerId) {
        players.putIfAbsent(playerId, new ConcurrentKeyedQueue<>());
    }

    void unregisterPlayer(UUID playerId) {
        players.remove(playerId);
    }

    void enqueue(UUID playerId, long chunkKey, T chunkBlocks) {
        players.computeIfPresent(playerId, (ignored, pending) -> {
            pending.add(chunkKey, chunkBlocks);
            return pending;
        });
    }

    T poll(UUID playerId, long chunkKey) {
        AtomicReference<T> result = new AtomicReference<>();
        players.computeIfPresent(playerId, (ignored, pending) -> {
            result.set(pending.poll(chunkKey));
            return pending;
        });
        return result.get();
    }

    void clear() {
        players.clear();
    }

    int pendingChunkKeyCount(UUID playerId) {
        ConcurrentKeyedQueue<Long, T> pending = players.get(playerId);
        return pending != null ? pending.keyCount() : 0;
    }

    boolean isPlayerRegistered(UUID playerId) {
        return players.containsKey(playerId);
    }
}

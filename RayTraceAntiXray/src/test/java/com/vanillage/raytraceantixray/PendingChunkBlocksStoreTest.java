package com.vanillage.raytraceantixray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PendingChunkBlocksStoreTest {
    @Test
    void ignoresLateEnqueueAfterPlayerUnregisters() {
        PendingChunkBlocksStore<String> store = new PendingChunkBlocksStore<>();
        UUID playerId = UUID.randomUUID();
        String chunkBlocks = new String("chunk");

        store.enqueue(playerId, 1L, chunkBlocks);
        assertNull(store.poll(playerId, 1L));

        store.registerPlayer(playerId);
        assertTrue(store.isPlayerRegistered(playerId));
        store.enqueue(playerId, 1L, chunkBlocks);
        store.unregisterPlayer(playerId);
        store.enqueue(playerId, 1L, chunkBlocks);

        assertFalse(store.isPlayerRegistered(playerId));
        assertNull(store.poll(playerId, 1L));
        assertEquals(0, store.pendingChunkKeyCount(playerId));
    }

    @Test
    void preservesFifoAndRemovesEmptyChunkQueue() {
        PendingChunkBlocksStore<String> store = new PendingChunkBlocksStore<>();
        UUID playerId = UUID.randomUUID();
        String first = new String("first");
        String second = new String("second");
        store.registerPlayer(playerId);

        store.enqueue(playerId, 7L, first);
        store.enqueue(playerId, 7L, second);

        assertEquals(1, store.pendingChunkKeyCount(playerId));
        assertSame(first, store.poll(playerId, 7L));
        assertSame(second, store.poll(playerId, 7L));
        assertEquals(0, store.pendingChunkKeyCount(playerId));
        assertTrue(store.isPlayerRegistered(playerId));
    }

    @Test
    void isolatesPlayersAndChunks() {
        PendingChunkBlocksStore<String> store = new PendingChunkBlocksStore<>();
        UUID firstPlayer = UUID.randomUUID();
        UUID secondPlayer = UUID.randomUUID();
        String first = new String("first");
        String second = new String("second");
        store.registerPlayer(firstPlayer);
        store.registerPlayer(secondPlayer);

        store.enqueue(firstPlayer, 1L, first);
        store.enqueue(secondPlayer, 2L, second);

        assertNull(store.poll(firstPlayer, 2L));
        assertSame(second, store.poll(secondPlayer, 2L));
        assertSame(first, store.poll(firstPlayer, 1L));
    }
}

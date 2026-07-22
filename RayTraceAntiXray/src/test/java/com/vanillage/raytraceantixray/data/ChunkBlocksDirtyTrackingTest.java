package com.vanillage.raytraceantixray.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;

class ChunkBlocksDirtyTrackingTest {

    @Test
    void newChunkStaysDirtyUntilSuccessfulTraceIsAcknowledged() {
        ChunkBlocks chunkBlocks = new ChunkBlocks(
            new WeakReference<>(null), new LongWrapper(12L), new ConcurrentHashMap<>(), true
        );

        assertTrue(chunkBlocks.isDirty());
        assertTrue(chunkBlocks.isDirty(), "a failed trace must leave the chunk dirty");

        chunkBlocks.markTraced();

        assertFalse(chunkBlocks.isDirty());
    }
}

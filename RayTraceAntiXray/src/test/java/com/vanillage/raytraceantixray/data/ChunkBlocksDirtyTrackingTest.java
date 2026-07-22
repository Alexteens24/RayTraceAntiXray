package com.vanillage.raytraceantixray.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;

class ChunkBlocksDirtyTrackingTest {

    @Test
    void newChunkIsDirtyUntilConsumed() {
        ChunkBlocks chunkBlocks = new ChunkBlocks(
            new WeakReference<>(null), new LongWrapper(12L), new ConcurrentHashMap<>(), true
        );

        assertTrue(chunkBlocks.setDirty(false));
        assertFalse(chunkBlocks.setDirty(false));
        assertFalse(chunkBlocks.setDirty(true));
        assertTrue(chunkBlocks.setDirty(false));
    }
}

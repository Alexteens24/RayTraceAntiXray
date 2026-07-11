package com.vanillage.raytraceantixray.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class LeafChunkTargetStoreTest {
    @Test
    void outOfOrderPollDoesNotConsumeAnotherChunkOrDimension() {
        LeafChunkTargetStore<String> store = new LeafChunkTargetStore<>();
        Object overworld = new Object();
        Object nether = new Object();
        store.add(overworld, 1L, "overworld-one");
        store.add(overworld, 2L, "overworld-two");
        store.add(nether, 1L, "nether-one");

        assertEquals("overworld-two", store.poll(overworld, 2L));
        assertEquals("nether-one", store.poll(nether, 1L));
        assertEquals("overworld-one", store.poll(overworld, 1L));
        assertEquals(0, store.keyCount());
    }

    @Test
    void repeatedChunkIsFifoAndCleanupRemovesEmptyKey() {
        LeafChunkTargetStore<String> store = new LeafChunkTargetStore<>();
        Object dimension = new Object();
        store.add(dimension, 3L, "first");
        store.add(dimension, 3L, "second");

        assertEquals("first", store.poll(dimension, 3L));
        assertEquals("second", store.poll(dimension, 3L));
        assertNull(store.poll(dimension, 3L));
        assertEquals(0, store.keyCount());
    }

    @Test
    void predicateCleanupPreservesOtherPlayers() {
        LeafChunkTargetStore<String> store = new LeafChunkTargetStore<>();
        Object dimension = new Object();
        store.add(dimension, 4L, "remove");
        store.add(dimension, 4L, "keep");

        store.removeIf("remove"::equals);

        assertEquals("keep", store.poll(dimension, 4L));
        assertEquals(0, store.keyCount());
    }
}

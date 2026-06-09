package com.vanillage.raytraceantixray.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/** CI runs without Leaf on the classpath; compat must stay inert on normal Paper builds. */
class LeafAsyncChunkSendCompatTest {

    @Test
    void inactiveOnStandardClasspath() {
        assertFalse(LeafAsyncChunkSendCompat.isLeafPresent(), "Leaf must be absent in unit-test / Paper CI");
        assertFalse(LeafAsyncChunkSendCompat.isActive());
        assertFalse(LeafAsyncChunkSendCompat.useLeafAsyncChunkSendPath());
    }
}

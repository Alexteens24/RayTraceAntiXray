package com.vanillage.raytraceantixray.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** CI runs without Leaf on the classpath; compat must stay inert on normal Paper builds. */
class LeafAsyncChunkSendCompatTest {

    @AfterEach
    void cleanUp() {
        LeafAsyncChunkSendCompat.shutdown();
    }

    @Test
    void inactiveOnStandardClasspath() {
        assertFalse(LeafAsyncChunkSendCompat.isLeafPresent(), "Leaf must be absent in unit-test / Paper CI");
        assertFalse(LeafAsyncChunkSendCompat.isActive());
        assertFalse(LeafAsyncChunkSendCompat.useLeafAsyncChunkSendPath());
    }

    @Test
    void validatesOnlyOneByOneThreadPoolExecutor() {
        ThreadPoolExecutor single = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        ThreadPoolExecutor multiple = new ThreadPoolExecutor(1, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        try {
            assertTrue(LeafAsyncChunkSendCompat.isVerifiedSingleThreadExecutor(single));
            assertFalse(LeafAsyncChunkSendCompat.isVerifiedSingleThreadExecutor(multiple));
            assertFalse(LeafAsyncChunkSendCompat.isVerifiedSingleThreadExecutor(new Object()));
        } finally {
            single.shutdownNow();
            multiple.shutdownNow();
        }
    }

    @Test
    void missingTargetWarningIsRateLimitedAndResetOnShutdown() {
        assertTrue(LeafAsyncChunkSendCompat.shouldLogMissingTargetWarning());
        assertFalse(LeafAsyncChunkSendCompat.shouldLogMissingTargetWarning());
        LeafAsyncChunkSendCompat.shutdown();
        assertTrue(LeafAsyncChunkSendCompat.shouldLogMissingTargetWarning());
    }
}

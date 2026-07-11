package com.vanillage.raytraceantixray.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

class ConcurrentKeyedQueueTest {
    @Test
    void pollingLastValueRemovesOnlyThatKey() {
        ConcurrentKeyedQueue<String, String> queue = new ConcurrentKeyedQueue<>();
        queue.add("a", "a1");
        queue.add("a", "a2");
        queue.add("b", "b1");

        assertEquals("b1", queue.poll("b"));
        assertEquals(1, queue.keyCount());
        assertEquals("a1", queue.poll("a"));
        assertEquals("a2", queue.poll("a"));
        assertEquals(0, queue.keyCount());
        assertNull(queue.poll("missing"));
    }

    @Test
    void removingValuesCleansEmptyKeysWithoutTouchingOtherQueues() {
        ConcurrentKeyedQueue<String, Integer> queue = new ConcurrentKeyedQueue<>();
        queue.add("first", 1);
        queue.add("first", 2);
        queue.add("second", 3);

        queue.removeValuesIf(value -> value <= 2);

        assertEquals(1, queue.keyCount());
        assertEquals(3, queue.poll("second"));
        assertEquals(0, queue.keyCount());
    }

    @Test
    void concurrentProducerAndConsumerLoseNoValues() {
        ConcurrentKeyedQueue<String, Integer> queue = new ConcurrentKeyedQueue<>();
        int count = 20_000;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> producer = executor.submit(() -> {
                await(start);
                for (int i = 0; i < count; i++) {
                    queue.add("chunk", i);
                }
            });
            Future<Integer> consumer = executor.submit(() -> {
                await(start);
                int received = 0;
                while (received < count) {
                    if (queue.poll("chunk") != null) {
                        received++;
                    } else {
                        Thread.onSpinWait();
                    }
                }
                return received;
            });

            start.countDown();
            org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
                producer.get();
                assertEquals(count, consumer.get());
            });
            assertEquals(0, queue.keyCount());
        } finally {
            executor.shutdownNow();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}

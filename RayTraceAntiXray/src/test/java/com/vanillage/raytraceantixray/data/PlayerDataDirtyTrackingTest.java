package com.vanillage.raytraceantixray.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.World;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

class PlayerDataDirtyTrackingTest {

    @Test
    void consumesOnlyChangedLocationSnapshots() {
        World world = mock(World.class);
        VectorialLocation first = location(world, 1.0, 0.0);
        PlayerData data = new PlayerData(new VectorialLocation[]{first});

        assertTrue(data.consumeLocationsDirty());
        assertFalse(data.consumeLocationsDirty());

        data.setLocations(new VectorialLocation[]{location(world, 1.0, 0.0)});
        assertFalse(data.consumeLocationsDirty(), "equal snapshots must not trigger another trace");

        data.setLocations(new VectorialLocation[]{location(world, 1.0, 0.25)});
        assertTrue(data.consumeLocationsDirty(), "a view-direction change must trigger tracing");
        assertFalse(data.consumeLocationsDirty());
    }

    @Test
    void addingChunkSetsAggregateDirtyFlagExactlyOnce() {
        PlayerData data = new PlayerData(new VectorialLocation[0]);
        LongWrapper key = new LongWrapper(42L);
        ChunkBlocks chunkBlocks = new ChunkBlocks(new WeakReference<>(null), key, new ConcurrentHashMap<>(), true);

        assertFalse(data.consumeChunksDirty());
        data.addChunk(chunkBlocks);

        assertSame(chunkBlocks, data.getChunks().get(key));
        assertTrue(data.consumeChunksDirty());
        assertFalse(data.consumeChunksDirty());
        assertSame(chunkBlocks, data.removeChunk(42L));
    }

    private static VectorialLocation location(World world, double x, double directionX) {
        return new VectorialLocation(world, new Vector(x, 2.0, 3.0), new Vector(directionX, 1.0, 0.0));
    }
}

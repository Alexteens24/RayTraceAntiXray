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
    void locationSnapshotStaysDirtyUntilSuccessfulTraceIsAcknowledged() {
        World world = mock(World.class);
        VectorialLocation first = location(world, 1.0, 0.0);
        PlayerData data = new PlayerData(new VectorialLocation[]{first});

        VectorialLocation[] initialSnapshot = data.getLocations();
        assertTrue(data.isLocationsDirty(initialSnapshot));
        assertTrue(data.isLocationsDirty(initialSnapshot), "a failed trace must leave the snapshot dirty");

        data.markLocationsTraced(initialSnapshot);
        assertFalse(data.isLocationsDirty(initialSnapshot));

        data.setLocations(new VectorialLocation[]{location(world, 1.0, 0.0)});
        assertFalse(data.isLocationsDirty(data.getLocations()), "equal snapshots must not trigger another trace");

        data.setLocations(new VectorialLocation[]{location(world, 1.0, 0.25)});
        VectorialLocation[] changedSnapshot = data.getLocations();
        assertTrue(data.isLocationsDirty(changedSnapshot), "a view-direction change must trigger tracing");

        data.markLocationsTraced(changedSnapshot);
        assertFalse(data.isLocationsDirty(changedSnapshot));
    }

    @Test
    void chunkRevisionCannotBeConsumedBeforeTraceSucceeds() {
        PlayerData data = new PlayerData(new VectorialLocation[0]);
        LongWrapper key = new LongWrapper(42L);
        ChunkBlocks chunkBlocks = new ChunkBlocks(new WeakReference<>(null), key, new ConcurrentHashMap<>(), true);

        long tracedRevision = data.getChunksRevision();
        data.addChunk(chunkBlocks);

        assertSame(chunkBlocks, data.getChunks().get(key));
        long dirtyRevision = data.getChunksRevision();
        assertTrue(dirtyRevision != tracedRevision);
        assertTrue(data.getChunksRevision() != tracedRevision, "a failed trace must preserve the newer revision");
        assertSame(chunkBlocks, data.removeChunk(42L));
    }

    private static VectorialLocation location(World world, double x, double directionX) {
        return new VectorialLocation(world, new Vector(x, 2.0, 3.0), new Vector(directionX, 1.0, 0.0));
    }
}

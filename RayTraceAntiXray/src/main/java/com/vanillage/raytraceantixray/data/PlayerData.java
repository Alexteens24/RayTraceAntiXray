package com.vanillage.raytraceantixray.data;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public final class PlayerData implements Callable<Object> {
    private static final VectorialLocation[] EMPTY_LOCATIONS = new VectorialLocation[0];

    private volatile VectorialLocation[] locations;
    private final AtomicReference<VectorialLocation[]> tracedLocations = new AtomicReference<>(EMPTY_LOCATIONS);
    private final ConcurrentMap<LongWrapper, ChunkBlocks> chunks = new ConcurrentHashMap<>();
    private final AtomicLong chunksRevision = new AtomicLong();
    private final Queue<Result> results = new ConcurrentLinkedQueue<>();
    private Callable<?> callable;
    /** Per-player block-update tick; cancelled on quit (Paper/Folia/Canvas region scheduler). */
    private volatile ScheduledTask blockUpdateTask;

    public PlayerData(VectorialLocation[] locations) {
        this.locations = locations;
    }

    public VectorialLocation[] getLocations() {
        return locations;
    }

    public void setLocations(VectorialLocation[] locations) {
        this.locations = locations;
    }

    /** Reports whether this snapshot differs from the last successfully traced snapshot. */
    public boolean isLocationsDirty(VectorialLocation[] snapshot) {
        return !Arrays.equals(tracedLocations.get(), snapshot);
    }

    /** Records a location snapshot only after its ray trace completed successfully. */
    public void markLocationsTraced(VectorialLocation[] snapshot) {
        tracedLocations.set(snapshot);
    }

    public ConcurrentMap<LongWrapper, ChunkBlocks> getChunks() {
        return chunks;
    }

    public void addChunk(ChunkBlocks chunkBlocks) {
        chunks.put(chunkBlocks.getKey(), chunkBlocks);
        chunksRevision.incrementAndGet();
    }

    public ChunkBlocks removeChunk(long chunkKey) {
        return chunks.remove(new LongWrapper(chunkKey));
    }

    public void clearChunks() {
        chunks.clear();
    }

    /** Monotonically identifies the current chunk-set snapshot. */
    public long getChunksRevision() {
        return chunksRevision.get();
    }

    public Queue<Result> getResults() {
        return results;
    }

    public Callable<?> getCallable() {
        return callable;
    }

    public void setCallable(Callable<?> callable) {
        this.callable = callable;
    }

    public ScheduledTask getBlockUpdateTask() {
        return blockUpdateTask;
    }

    public void setBlockUpdateTask(ScheduledTask blockUpdateTask) {
        this.blockUpdateTask = blockUpdateTask;
    }

    @Override
    public Object call() throws Exception {
        return callable.call();
    }
}

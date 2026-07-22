package com.vanillage.raytraceantixray.data;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public final class PlayerData implements Callable<Object> {
    private static final VectorialLocation[] EMPTY_LOCATIONS = new VectorialLocation[0];

    private volatile VectorialLocation[] locations;
    private final AtomicReference<VectorialLocation[]> tracedLocations = new AtomicReference<>(EMPTY_LOCATIONS);
    private final ConcurrentMap<LongWrapper, ChunkBlocks> chunks = new ConcurrentHashMap<>();
    private final AtomicBoolean chunksDirty = new AtomicBoolean();
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

    /** Records the current location snapshot as traced and reports whether it changed. */
    public boolean consumeLocationsDirty() {
        VectorialLocation[] current = locations;
        VectorialLocation[] previous = tracedLocations.getAndSet(current);
        return !Arrays.equals(previous, current);
    }

    public ConcurrentMap<LongWrapper, ChunkBlocks> getChunks() {
        return chunks;
    }

    public void addChunk(ChunkBlocks chunkBlocks) {
        chunks.put(chunkBlocks.getKey(), chunkBlocks);
        chunksDirty.set(true);
    }

    public ChunkBlocks removeChunk(long chunkKey) {
        return chunks.remove(new LongWrapper(chunkKey));
    }

    public void clearChunks() {
        chunks.clear();
    }

    /** Clears the aggregate chunk-dirty flag and returns its previous value. */
    public boolean consumeChunksDirty() {
        return chunksDirty.getAndSet(false);
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

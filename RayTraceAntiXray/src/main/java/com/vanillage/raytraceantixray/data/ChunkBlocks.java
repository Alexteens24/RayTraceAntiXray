package com.vanillage.raytraceantixray.data;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.core.BlockPos;
import com.vanillage.raytraceantixray.nms.NmsCompat;
import net.minecraft.world.level.chunk.LevelChunk;

public final class ChunkBlocks {
    private final Reference<LevelChunk> chunk;
    private final LongWrapper key;
    private final Map<BlockPos, Boolean> blocks;
    private final AtomicBoolean dirty;

    public ChunkBlocks(LevelChunk chunk, Map<BlockPos, Boolean> blocks) {
        this(chunk, blocks, true);
    }

    public ChunkBlocks(LevelChunk chunk, Map<BlockPos, Boolean> blocks, boolean dirty) {
        this(new WeakReference<>(chunk), new LongWrapper(NmsCompat.chunkPosKey(chunk.getPos())), blocks, dirty);
    }

    ChunkBlocks(Reference<LevelChunk> chunk, LongWrapper key, Map<BlockPos, Boolean> blocks, boolean dirty) {
        this.chunk = chunk;
        this.key = key;
        this.blocks = blocks;
        this.dirty = new AtomicBoolean(dirty);
    }

    public LevelChunk getChunk() {
        return chunk.get();
    }

    public LongWrapper getKey() {
        return key;
    }

    public Map<BlockPos, Boolean> getBlocks() {
        return blocks;
    }

    /** Sets the dirty flag and returns its previous value. */
    public boolean setDirty(boolean dirty) {
        return this.dirty.getAndSet(dirty);
    }
}

package com.vanillage.raytraceantixray.nms.paper_26_3;

import com.vanillage.raytraceantixray.antixray.ChunkPacketInfoAntiXray;
import com.vanillage.raytraceantixray.antixray.ChunkPacketInfoAntiXrayState;
import io.papermc.paper.antixray.ChunkPacketInfo;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Paper 26.3 chunk packet state. {@code ChunkPacketInfo} is built from the chunk alone; the packet is
 * attached by {@code ChunkPacketInfo#setChunkPacket} right after the controller created this instance.
 */
public final class ChunkPacketInfoAntiXray26_3 extends ChunkPacketInfo<BlockState> implements ChunkPacketInfoAntiXray {

    private final ChunkPacketInfoAntiXrayState state;

    public ChunkPacketInfoAntiXray26_3(LevelChunk chunk, ChunkPacketInfoAntiXrayState state) {
        super(chunk);
        this.state = state;
    }

    @Override
    public ChunkPacketInfoAntiXrayState state() {
        return state;
    }
}

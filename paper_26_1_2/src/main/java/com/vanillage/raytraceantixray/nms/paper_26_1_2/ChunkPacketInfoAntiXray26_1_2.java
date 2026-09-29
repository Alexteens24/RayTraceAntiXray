package com.vanillage.raytraceantixray.nms.paper_26_1_2;

import com.vanillage.raytraceantixray.antixray.ChunkPacketInfoAntiXray;
import com.vanillage.raytraceantixray.antixray.ChunkPacketInfoAntiXrayState;
import io.papermc.paper.antixray.ChunkPacketInfo;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/** Paper 26.1.2 chunk packet state; {@code ChunkPacketInfo} still takes the packet in its constructor. */
public final class ChunkPacketInfoAntiXray26_1_2 extends ChunkPacketInfo<BlockState> implements ChunkPacketInfoAntiXray {

    private final ChunkPacketInfoAntiXrayState state;

    public ChunkPacketInfoAntiXray26_1_2(ClientboundLevelChunkWithLightPacket chunkPacket, LevelChunk chunk, ChunkPacketInfoAntiXrayState state) {
        super(chunkPacket, chunk);
        this.state = state;
    }

    @Override
    public ChunkPacketInfoAntiXrayState state() {
        return state;
    }
}

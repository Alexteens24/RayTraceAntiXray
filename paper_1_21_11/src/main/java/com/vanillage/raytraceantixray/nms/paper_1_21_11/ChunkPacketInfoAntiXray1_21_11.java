package com.vanillage.raytraceantixray.nms.paper_1_21_11;

import com.vanillage.raytraceantixray.antixray.ChunkPacketInfoAntiXray;
import com.vanillage.raytraceantixray.antixray.ChunkPacketInfoAntiXrayState;
import io.papermc.paper.antixray.ChunkPacketInfo;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/** Paper 1.21.11 chunk packet state; {@code ChunkPacketInfo} still takes the packet in its constructor. */
public final class ChunkPacketInfoAntiXray1_21_11 extends ChunkPacketInfo<BlockState> implements ChunkPacketInfoAntiXray {

    private final ChunkPacketInfoAntiXrayState state;

    public ChunkPacketInfoAntiXray1_21_11(ClientboundLevelChunkWithLightPacket chunkPacket, LevelChunk chunk, ChunkPacketInfoAntiXrayState state) {
        super(chunkPacket, chunk);
        this.state = state;
    }

    @Override
    public ChunkPacketInfoAntiXrayState state() {
        return state;
    }
}

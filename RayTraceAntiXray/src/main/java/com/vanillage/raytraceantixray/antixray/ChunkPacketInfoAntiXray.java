package com.vanillage.raytraceantixray.antixray;

import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.Palette;

/**
 * Version independent view of the Paper Anti-Xray chunk packet state.
 *
 * <p>Paper 26.3 changed {@code ChunkPacketInfo} to be created from the chunk alone with the packet attached
 * afterwards, while 1.21.11 / 26.1.2 / 26.2 still take the packet in the constructor. The concrete subclass
 * therefore lives in the per-version NMS modules; every accessor below is unchanged across those versions,
 * so each implementation only has to inherit them from {@code ChunkPacketInfo}.
 */
public interface ChunkPacketInfoAntiXray extends Runnable {

    LevelChunk getChunk();

    byte[] getBuffer();

    void setBuffer(byte[] buffer);

    int getBits(int index);

    int getIndex(int index);

    boolean isWritten(int index);

    Palette<BlockState> getPalette(int index);

    BlockState[] getPresetValues(int index);

    ClientboundLevelChunkWithLightPacket getChunkPacket();

    ChunkPacketInfoAntiXrayState state();

    default ServerPlayer getTargetPlayer() {
        return state().targetPlayer();
    }

    default LevelChunk[] getNearbyChunks() {
        return state().nearbyChunks();
    }

    default void setNearbyChunks(LevelChunk... nearbyChunks) {
        state().nearbyChunks(nearbyChunks);
    }

    @Override
    default void run() {
        state().controller().obfuscate(this);
    }
}

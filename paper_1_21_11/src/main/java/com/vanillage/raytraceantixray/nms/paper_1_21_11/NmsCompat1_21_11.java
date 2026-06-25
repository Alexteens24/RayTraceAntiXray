package com.vanillage.raytraceantixray.nms.paper_1_21_11;

import com.vanillage.raytraceantixray.nms.NmsBridge;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;

/** Paper 1.21.11 NMS bindings (loaded at runtime via {@link NmsBridge}). */
public final class NmsCompat1_21_11 implements NmsBridge {

    @Override
    public long chunkKey(int chunkX, int chunkZ) {
        return ChunkPos.asLong(chunkX, chunkZ);
    }

    @Override
    public long chunkPosKey(ChunkPos chunkPos) {
        return chunkPos.toLong();
    }

    @Override
    public int chunkX(ChunkPos chunkPos) {
        return chunkPos.x;
    }

    @Override
    public int chunkZ(ChunkPos chunkPos) {
        return chunkPos.z;
    }

    @Override
    public boolean isConnectionDisconnected(ServerGamePacketListenerImpl connection) {
        return connection.processedDisconnect;
    }
}

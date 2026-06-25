package com.vanillage.raytraceantixray.nms.paper_26_1_2;

import com.vanillage.raytraceantixray.nms.NmsBridge;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;

/** Paper 26.1.2 NMS bindings (loaded at runtime via {@link NmsBridge}). */
public final class NmsCompat26_1_2 implements NmsBridge {

    @Override
    public long chunkKey(int chunkX, int chunkZ) {
        return ChunkPos.pack(chunkX, chunkZ);
    }

    @Override
    public long chunkPosKey(ChunkPos chunkPos) {
        return chunkPos.pack();
    }

    @Override
    public int chunkX(ChunkPos chunkPos) {
        return chunkPos.x();
    }

    @Override
    public int chunkZ(ChunkPos chunkPos) {
        return chunkPos.z();
    }

    @Override
    public boolean isConnectionDisconnected(ServerGamePacketListenerImpl connection) {
        return connection.isDisconnected();
    }
}

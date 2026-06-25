package com.vanillage.raytraceantixray.nms;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;

/** Static facade over the runtime-selected {@link NmsBridge} implementation. */
public final class NmsCompat {
    private NmsCompat() {
    }

    private static NmsBridge nms() {
        return NmsBridge.get();
    }

    public static long chunkKey(int chunkX, int chunkZ) {
        return nms().chunkKey(chunkX, chunkZ);
    }

    public static long chunkPosKey(ChunkPos chunkPos) {
        return nms().chunkPosKey(chunkPos);
    }

    public static int chunkX(ChunkPos chunkPos) {
        return nms().chunkX(chunkPos);
    }

    public static int chunkZ(ChunkPos chunkPos) {
        return nms().chunkZ(chunkPos);
    }

    public static boolean isConnectionDisconnected(ServerGamePacketListenerImpl connection) {
        return nms().isConnectionDisconnected(connection);
    }
}

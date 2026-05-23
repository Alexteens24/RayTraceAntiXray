package com.vanillage.raytraceantixray.nms;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;

/** Paper 1.21.11 NMS bindings (selected at compile time via {@code -PpaperTarget=1.21.11}). */
public final class NmsCompat {
    private NmsCompat() {
    }

    public static long chunkKey(int chunkX, int chunkZ) {
        return ChunkPos.asLong(chunkX, chunkZ);
    }

    public static long chunkPosKey(ChunkPos chunkPos) {
        return chunkPos.toLong();
    }

    public static int chunkX(ChunkPos chunkPos) {
        return chunkPos.x;
    }

    public static int chunkZ(ChunkPos chunkPos) {
        return chunkPos.z;
    }

    public static boolean isConnectionDisconnected(ServerGamePacketListenerImpl connection) {
        return connection.processedDisconnect;
    }
}

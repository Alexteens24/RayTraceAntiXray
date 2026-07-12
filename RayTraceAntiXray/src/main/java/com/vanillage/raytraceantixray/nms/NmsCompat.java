package com.vanillage.raytraceantixray.nms;

import java.util.concurrent.Executor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

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

    public static Executor serverExecutor(MinecraftServer server) {
        return nms().serverExecutor(server);
    }

    public static Level gameModeLevel(ServerPlayerGameMode gameMode) {
        return nms().gameModeLevel(gameMode);
    }
}

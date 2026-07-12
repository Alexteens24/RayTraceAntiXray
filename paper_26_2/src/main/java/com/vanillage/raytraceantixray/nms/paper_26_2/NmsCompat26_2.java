package com.vanillage.raytraceantixray.nms.paper_26_2;

import com.vanillage.raytraceantixray.nms.NmsBridge;
import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/** Paper 26.2 NMS bindings (loaded at runtime via {@link NmsBridge}). */
public final class NmsCompat26_2 implements NmsBridge {
    private static final Field SERVER_EXECUTOR_FIELD = findServerExecutorField();

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

    @Override
    public Executor serverExecutor(MinecraftServer server) {
        try {
            return (Executor) SERVER_EXECUTOR_FIELD.get(server);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read Paper 26.2 MinecraftServer.executor", e);
        }
    }

    @Override
    public Level gameModeLevel(ServerPlayerGameMode gameMode) {
        throw new UnsupportedOperationException("Paper 26.2 supplies Level directly to onPlayerLeftClickBlock");
    }

    private static Field findServerExecutorField() {
        try {
            Field field = MinecraftServer.class.getDeclaredField("executor");
            if (!field.trySetAccessible()) {
                throw new IllegalStateException("Paper 26.2 MinecraftServer.executor is not accessible");
            }
            return field;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Paper 26.2 MinecraftServer.executor was not found", e);
        }
    }
}

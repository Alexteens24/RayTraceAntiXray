package com.vanillage.raytraceantixray.nms.paper_26_1_2;

import com.vanillage.raytraceantixray.nms.NmsBridge;
import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;


public final class NmsCompat26_1_2 implements NmsBridge {
    private static final Field GAME_MODE_LEVEL_FIELD = findField(ServerPlayerGameMode.class, "level");

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
        return server.executor;
    }

    @Override
    public Level gameModeLevel(ServerPlayerGameMode gameMode) {
        try {
            return (Level) GAME_MODE_LEVEL_FIELD.get(gameMode);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read ServerPlayerGameMode.level", e);
        }
    }

    private static Field findField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(owner.getName() + "." + name + " was not found", e);
        }
    }
}

package com.vanillage.raytraceantixray.nms.paper_1_21_11;

import com.vanillage.raytraceantixray.antixray.ChunkPacketInfoAntiXrayState;
import com.vanillage.raytraceantixray.nms.NmsBridge;
import io.papermc.paper.antixray.ChunkPacketInfo;
import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;


public final class NmsCompat1_21_11 implements NmsBridge {
    private static final Field GAME_MODE_LEVEL_FIELD = findField(ServerPlayerGameMode.class, "level");

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

    @Override
    public ChunkPacketInfo<BlockState> createChunkPacketInfo(ChunkPacketInfoAntiXrayState state, @Nullable ClientboundLevelChunkWithLightPacket chunkPacket, LevelChunk chunk) {
        if (chunkPacket == null) {
            throw new IllegalStateException("Paper 1.21.11 requires the chunk packet to build the chunk packet state");
        }
        return new ChunkPacketInfoAntiXray1_21_11(chunkPacket, chunk, state);
    }

    @Override
    public ClientboundLevelChunkPacketData chunkPacketData(ClientboundLevelChunkWithLightPacket chunkPacket) {
        return chunkPacket.getChunkData();
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

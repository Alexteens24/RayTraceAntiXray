package com.vanillage.raytraceantixray.nms.paper_26_3;

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


/** Paper 26.3 NMS bindings (loaded at runtime via {@link NmsBridge}). */
public final class NmsCompat26_3 implements NmsBridge {
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
            throw new IllegalStateException("Cannot read Paper 26.3 MinecraftServer.executor", e);
        }
    }

    @Override
    public Level gameModeLevel(ServerPlayerGameMode gameMode) {
        throw new UnsupportedOperationException("Paper 26.3 supplies Level directly to onPlayerLeftClickBlock");
    }

    @Override
    public ChunkPacketInfo<BlockState> createChunkPacketInfo(ChunkPacketInfoAntiXrayState state, @Nullable ClientboundLevelChunkWithLightPacket chunkPacket, LevelChunk chunk) {
        return new ChunkPacketInfoAntiXray26_3(chunk, state);
    }

    @Override
    public ClientboundLevelChunkPacketData chunkPacketData(ClientboundLevelChunkWithLightPacket chunkPacket) {
        return chunkPacket.chunkData();
    }

    private static Field findServerExecutorField() {
        try {
            Field field = MinecraftServer.class.getDeclaredField("executor");
            if (!field.trySetAccessible()) {
                throw new IllegalStateException("Paper 26.3 MinecraftServer.executor is not accessible");
            }
            return field;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Paper 26.3 MinecraftServer.executor was not found", e);
        }
    }
}

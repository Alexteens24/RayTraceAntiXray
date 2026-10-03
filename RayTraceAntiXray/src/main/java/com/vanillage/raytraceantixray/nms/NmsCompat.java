package com.vanillage.raytraceantixray.nms;

import com.vanillage.raytraceantixray.antixray.ChunkPacketInfoAntiXrayState;
import io.papermc.paper.antixray.ChunkPacketInfo;
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

    public static ChunkPacketInfo<BlockState> createChunkPacketInfo(ChunkPacketInfoAntiXrayState state, @Nullable ClientboundLevelChunkWithLightPacket chunkPacket, LevelChunk chunk) {
        return nms().createChunkPacketInfo(state, chunkPacket, chunk);
    }

    public static ClientboundLevelChunkPacketData chunkPacketData(ClientboundLevelChunkWithLightPacket chunkPacket) {
        return nms().chunkPacketData(chunkPacket);
    }

    /** Minecraft version of the running server, for diagnostics and error messages. Never throws. */
    public static String detectedMinecraftVersion() {
        return NmsBridge.detectedMinecraftVersion();
    }
}

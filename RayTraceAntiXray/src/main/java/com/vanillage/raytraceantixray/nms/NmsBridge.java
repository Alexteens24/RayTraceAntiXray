package com.vanillage.raytraceantixray.nms;

import com.vanillage.raytraceantixray.antixray.ChunkPacketInfoAntiXrayState;
import io.papermc.paper.antixray.ChunkPacketInfo;
import java.lang.reflect.Method;
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
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;


public interface NmsBridge {

    long chunkKey(int chunkX, int chunkZ);

    long chunkPosKey(ChunkPos chunkPos);

    int chunkX(ChunkPos chunkPos);

    int chunkZ(ChunkPos chunkPos);

    boolean isConnectionDisconnected(ServerGamePacketListenerImpl connection);

    Executor serverExecutor(MinecraftServer server);

    Level gameModeLevel(ServerPlayerGameMode gameMode);

    /**
     * Creates the {@code ChunkPacketInfo} subclass for this Paper version.
     *
     * <p>Paper 26.3 builds the chunk packet from the chunk alone and attaches the packet afterwards, so
     * {@code chunkPacket} is {@code null} on that version and the packet is never read here.
     */
    ChunkPacketInfo<BlockState> createChunkPacketInfo(ChunkPacketInfoAntiXrayState state, @Nullable ClientboundLevelChunkWithLightPacket chunkPacket, LevelChunk chunk);

    /** Paper 26.3 renamed {@code getChunkData()} to the record accessor {@code chunkData()}. */
    ClientboundLevelChunkPacketData chunkPacketData(ClientboundLevelChunkWithLightPacket chunkPacket);

    static NmsBridge get() {
        return Holder.INSTANCE;
    }

    /**
     * Best-effort Minecraft version of the running server, for diagnostics.
     *
     * <p>Never throws: callers use this to build human-readable messages, and a failure to read the version
     * must not mask the original problem.
     */
    static String detectedMinecraftVersion() {
        String version = minecraftVersionFromServerBuildInfo();
        if (version != null) {
            return version;
        }
        try {
            return Bukkit.getServer().getMinecraftVersion();
        } catch (final Throwable e) {
            return "unknown";
        }
    }

    final class Holder {
        private static final NmsBridge INSTANCE;

        static {
            final String minecraftVersion = detectedMinecraftVersion();
            final String underscored = minecraftVersion.replace('.', '_');
            final String className = "com.vanillage.raytraceantixray.nms.paper_"
                + underscored + ".NmsCompat" + underscored;
            try {
                INSTANCE = (NmsBridge) Class.forName(className).getDeclaredConstructors()[0].newInstance();
            } catch (final ReflectiveOperationException e) {
                throw new IllegalStateException(
                    "RayTraceAntiXray does not support Minecraft " + minecraftVersion
                        + " (no NMS bindings at " + className + ")", e);
            }
        }
    }

    private static @Nullable String minecraftVersionFromServerBuildInfo() {
        try {
            final Class<?> cls = Class.forName("io.papermc.paper.ServerBuildInfo");
            final Method method = cls.getMethod("minecraftVersionId");
            final Object instance = cls.getMethod("buildInfo").invoke(null);
            return (String) method.invoke(instance);
        } catch (final Throwable e) {
            return null;
        }
    }
}

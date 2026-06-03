package com.vanillage.raytraceantixray.compat;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import com.vanillage.raytraceantixray.nms.NmsCompat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Runtime compatibility for Leaf's {@code async-chunk-send} feature.
 * <p>
 * When enabled, Leaf builds {@code ClientboundLevelChunkWithLightPacket} on a dedicated async thread
 * and calls {@code leaf$modifyBlocks} instead of {@code modifyBlocks}. {@code shouldModify} still runs on
 * the server thread, so Paper's ThreadLocal player hand-off does not work; we use a FIFO queue instead.
 */
public final class LeafAsyncChunkSendCompat {

    private static final String ASYNC_CHUNK_SEND_CLASS = "org.dreeam.leaf.config.modules.async.AsyncChunkSend";
    private static final boolean LEAF_PRESENT = classPresent(ASYNC_CHUNK_SEND_CLASS);

    private static final ConcurrentLinkedQueue<PendingChunkTarget> PENDING_TARGETS = new ConcurrentLinkedQueue<>();

    private LeafAsyncChunkSendCompat() {
    }

    public static boolean isLeafPresent() {
        return LEAF_PRESENT;
    }

    /** {@code true} when Leaf is on the classpath and {@code async-chunk-send.enabled} is {@code true}. */
    public static boolean isActive() {
        if (!LEAF_PRESENT) {
            return false;
        }
        try {
            return Class.forName(ASYNC_CHUNK_SEND_CLASS).getField("enabled").getBoolean(null);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    public static void logStatus(Logger logger) {
        if (!LEAF_PRESENT) {
            return;
        }
        if (isActive()) {
            logger.info("Leaf async-chunk-send is enabled; using Leaf async chunk-send compatibility layer.");
        } else {
            logger.info("Leaf detected; async-chunk-send is disabled (standard Paper chunk-send path).");
        }
    }

    /**
     * Called from {@code shouldModify} on the server thread when obfuscation will run for this send.
     */
    public static void onShouldModify(ServerPlayer player, LevelChunk chunk) {
        if (!isActive()) {
            return;
        }
        PENDING_TARGETS.add(new PendingChunkTarget(player, NmsCompat.chunkX(chunk.getPos()), NmsCompat.chunkZ(chunk.getPos())));
    }

    /**
     * Resolves the recipient player for {@code getChunkPacketInfo} on Leaf's async chunk-send thread.
     */
    public static ServerPlayer pollTargetPlayer(LevelChunk chunk, Logger logger) {
        if (!isActive()) {
            return null;
        }
        int chunkX = NmsCompat.chunkX(chunk.getPos());
        int chunkZ = NmsCompat.chunkZ(chunk.getPos());
        PendingChunkTarget pending;
        while ((pending = PENDING_TARGETS.poll()) != null) {
            if (pending.chunkX() == chunkX && pending.chunkZ() == chunkZ) {
                return pending.player();
            }
            logger.warning("RayTraceAntiXray: Leaf async-chunk-send target queue desync "
                + "(expected chunk " + chunkX + "," + chunkZ + " but got " + pending.chunkX() + "," + pending.chunkZ() + "); "
                + "ray tracing may miss blocks for that chunk.");
        }
        logger.warning("RayTraceAntiXray: no Leaf async-chunk-send target for chunk " + chunkX + "," + chunkZ
            + "; ray tracing may miss this chunk.");
        return null;
    }

    private static boolean classPresent(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private record PendingChunkTarget(ServerPlayer player, int chunkX, int chunkZ) {
    }
}

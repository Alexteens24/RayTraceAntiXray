package com.vanillage.raytraceantixray.compat;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import com.vanillage.raytraceantixray.nms.NmsCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;


public final class LeafAsyncChunkSendCompat {
    private static final String CONFIG_CLASS = "org.dreeam.leaf.config.modules.async.AsyncChunkSend";
    private static final String EXECUTOR_CLASS = "org.dreeam.leaf.async.chunk.AsyncChunkSend";
    private static final long WARNING_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(60L);
    private static final boolean LEAF_PRESENT = classPresent(CONFIG_CLASS);
    private static final ConcurrentHashMap<UUID, Boolean> REGISTERED_PLAYERS = new ConcurrentHashMap<>();
    private static final LeafChunkTargetStore<PendingChunkTarget> PENDING_TARGETS = new LeafChunkTargetStore<>();
    private static final AtomicLong LAST_MISSING_TARGET_WARNING = new AtomicLong(Long.MIN_VALUE);
    private static volatile RuntimeState runtimeState = LEAF_PRESENT ? RuntimeState.UNINITIALIZED : RuntimeState.ABSENT;

    private LeafAsyncChunkSendCompat() {
    }


    public static synchronized void initialize(Logger logger) {
        runtimeState = detectRuntimeState();
        switch (runtimeState) {
            case ABSENT -> {
                return;
            }
            case DISABLED -> logger.info("Leaf detected; async-chunk-send is disabled (standard Paper chunk-send path).");
            case SUPPORTED_SINGLE_THREAD -> logger.info("Leaf async-chunk-send is enabled with a single worker; using keyed chunk-send target queues.");
            case UNSUPPORTED_WORKER -> logger.severe("Leaf async-chunk-send uses an unsupported or unrecognized worker model. "
                + "Ray-trace chunk association is disabled to avoid assigning chunk data to the wrong player; "
                + "disable async-chunk-send in Leaf configuration.");
            case UNINITIALIZED -> throw new IllegalStateException("Leaf runtime detection did not initialize");
        }
    }

    public static boolean isLeafPresent() {
        return LEAF_PRESENT;
    }


    public static boolean isActive() {
        RuntimeState state = runtimeState;
        return state == RuntimeState.SUPPORTED_SINGLE_THREAD || state == RuntimeState.UNSUPPORTED_WORKER;
    }


    public static boolean useLeafAsyncChunkSendPath() {
        return isActive();
    }


    public static boolean canAssociateTargets() {
        return runtimeState == RuntimeState.SUPPORTED_SINGLE_THREAD;
    }

    public static void registerPlayer(UUID playerId) {
        REGISTERED_PLAYERS.putIfAbsent(playerId, Boolean.TRUE);
    }

    public static void unregisterPlayer(UUID playerId) {
        REGISTERED_PLAYERS.remove(playerId);
        PENDING_TARGETS.removeIf(target -> target.playerId().equals(playerId));
    }

    public static void shutdown() {
        REGISTERED_PLAYERS.clear();
        PENDING_TARGETS.clear();
        LAST_MISSING_TARGET_WARNING.set(Long.MIN_VALUE);
    }


    public static void onShouldModify(ServerPlayer player, LevelChunk chunk) {
        UUID playerId = player.getUUID();
        if (!canAssociateTargets()) {
            return;
        }
        REGISTERED_PLAYERS.computeIfPresent(playerId, (ignored, registered) -> {
            PENDING_TARGETS.add(chunk.getLevel().dimension(), NmsCompat.chunkPosKey(chunk.getPos()), new PendingChunkTarget(playerId, player));
            return registered;
        });
    }


    public static ServerPlayer pollTargetPlayer(LevelChunk chunk, Logger logger) {
        if (!canAssociateTargets()) {
            return null;
        }
        PendingChunkTarget target = PENDING_TARGETS.poll(chunk.getLevel().dimension(), NmsCompat.chunkPosKey(chunk.getPos()));
        if (target != null) {
            return target.player();
        }
        warnMissingTarget(logger, "RayTraceAntiXray: no Leaf async-chunk-send target for chunk "
            + NmsCompat.chunkX(chunk.getPos()) + "," + NmsCompat.chunkZ(chunk.getPos())
            + "; ray tracing may miss this chunk.");
        return null;
    }


    public static boolean shouldLogMissingTargetWarning() {
        if (runtimeState == RuntimeState.UNSUPPORTED_WORKER) {
            return false;
        }
        long now = System.nanoTime();
        long previous = LAST_MISSING_TARGET_WARNING.get();
        return (previous == Long.MIN_VALUE || now - previous >= WARNING_INTERVAL_NANOS)
            && LAST_MISSING_TARGET_WARNING.compareAndSet(previous, now);
    }

    private static void warnMissingTarget(Logger logger, String message) {
        if (shouldLogMissingTargetWarning()) {
            logger.warning(message);
        }
    }

    private static RuntimeState detectRuntimeState() {
        if (!LEAF_PRESENT) {
            return RuntimeState.ABSENT;
        }
        try {
            if (!Class.forName(CONFIG_CLASS).getField("enabled").getBoolean(null)) {
                return RuntimeState.DISABLED;
            }
            Field poolField = Class.forName(EXECUTOR_CLASS).getField("POOL");
            return isVerifiedSingleThreadExecutor(poolField.get(null))
                ? RuntimeState.SUPPORTED_SINGLE_THREAD
                : RuntimeState.UNSUPPORTED_WORKER;
        } catch (ReflectiveOperationException | LinkageError e) {
            return RuntimeState.UNSUPPORTED_WORKER;
        }
    }

    static boolean isVerifiedSingleThreadExecutor(Object executor) {
        return executor instanceof ThreadPoolExecutor pool
            && pool.getCorePoolSize() == 1
            && pool.getMaximumPoolSize() == 1;
    }

    private static boolean classPresent(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    enum RuntimeState {
        UNINITIALIZED,
        ABSENT,
        DISABLED,
        SUPPORTED_SINGLE_THREAD,
        UNSUPPORTED_WORKER
    }

    private record PendingChunkTarget(UUID playerId, ServerPlayer player) {
    }
}

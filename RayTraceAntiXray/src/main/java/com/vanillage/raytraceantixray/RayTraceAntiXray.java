package com.vanillage.raytraceantixray;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vanillage.raytraceantixray.antixray.ChunkPacketBlockControllerAntiXray;
import com.vanillage.raytraceantixray.commands.RayTraceAntiXrayTabExecutor;
import com.vanillage.raytraceantixray.data.ChunkBlocks;
import com.vanillage.raytraceantixray.data.PlayerData;
import com.vanillage.raytraceantixray.data.VectorialLocation;
import com.vanillage.raytraceantixray.listeners.PacketListener;
import com.vanillage.raytraceantixray.listeners.PlayerListener;
import com.vanillage.raytraceantixray.listeners.WorldListener;
import com.vanillage.raytraceantixray.metrics.RayTraceAntiXrayMetrics;
import com.vanillage.raytraceantixray.tasks.RayTraceTimerTask;

import io.papermc.paper.antixray.ChunkPacketBlockController;
import io.papermc.paper.configuration.WorldConfiguration.Anticheat.AntiXray;
import io.papermc.paper.configuration.type.EngineMode;
import net.minecraft.server.level.ServerLevel;
import com.vanillage.raytraceantixray.nms.NmsCompat;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public final class RayTraceAntiXray extends JavaPlugin implements RayTraceAntiXrayCommandTarget {
    // private volatile Configuration configuration;
    private boolean folia = false;
    private volatile boolean running = false;
    private volatile boolean timingsEnabled = false;
    /** Pending obfuscated chunk payloads keyed by player then chunk column (see {@link #enqueuePendingChunkBlocks}). */
    private final ConcurrentMap<UUID, ConcurrentMap<Long, ConcurrentLinkedQueue<ChunkBlocks>>> pendingChunkBlocksByPlayer = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, PlayerData> playerData = new ConcurrentHashMap<>();
    private ExecutorService executorService;
    private ScheduledTask rayTraceScheduledTask;
    private long updateTicks = 1L;
    private PacketListenerCommon packetEventsChunkListener;

    @Override
    public void onEnable() {
        if (!new File(getDataFolder(), "README.txt").exists()) {
            saveResource("README.txt", false);
        }

        saveDefaultConfig();
        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);
        // Add defaults.
        // saveConfig();
        // configuration = config;
        // Initialize stuff.

        // Folia-only type: not on Paper compile classpath when using paperDevBundle (Paper). Detect at runtime.
        try {
            Class<?> regionizedServer = Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = regionizedServer.isInstance(getServer());
        } catch (ClassNotFoundException e) {
            folia = false;
        }

        running = true;
        RayTraceAntiXrayMetrics.register(this, folia, config);
        startRayTraceSchedulerFromConfig(config);

        // Block updates run per-player via PlayerListener + EntityScheduler (required on Folia/Canvas for world reads).

        // Register events.
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new WorldListener(this), this);
        pluginManager.registerEvents(new PlayerListener(this), this);
        PlayerListener.registerExistingPlayers(this);
        // Worlds load before plugins enable; WorldInitEvent already ran — patch controllers for existing worlds.
        for (World w : Bukkit.getWorlds()) {
            WorldListener.handleLoad(this, w);
        }

        packetEventsChunkListener = new PacketListener(this);
        PacketEvents.getAPI().getEventManager().registerListener(packetEventsChunkListener);
        // registerCommands();
        getCommand("raytraceantixray").setExecutor(new RayTraceAntiXrayTabExecutor(this));
        getLogger().info(getPluginMeta().getDisplayName() + " enabled");
    }

    @Override
    public void onDisable() {
        Throwable throwable = null;

        if (packetEventsChunkListener != null) {
            try {
                PacketEvents.getAPI().getEventManager().unregisterListener(packetEventsChunkListener);
                packetEventsChunkListener = null;
            } catch (Throwable ignored) {
                // PacketEvents may already be torn down during shutdown.
            }
        }

        try {
            running = false;
            if (rayTraceScheduledTask != null) {
                rayTraceScheduledTask.cancel();
                rayTraceScheduledTask = null;
            }
        } catch (Throwable t) {
            throwable = mergeThrowables(throwable, t);
        }

        try {
            if (executorService != null) {
                executorService.shutdownNow();
                executorService.awaitTermination(1000L, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throwable = mergeThrowables(throwable, new RuntimeException(e));
        } catch (Throwable t) {
            throwable = mergeThrowables(throwable, t);
        }

        try {
            pendingChunkBlocksByPlayer.clear();
            for (PlayerData pd : playerData.values()) {
                if (pd.getBlockUpdateTask() != null) {
                    pd.getBlockUpdateTask().cancel();
                }
            }
            playerData.clear();
        } catch (Throwable t) {
            throwable = mergeThrowables(throwable, t);
        }

        if (throwable != null) {
            Throwables.throwIfUnchecked(throwable);
            throw new RuntimeException(throwable);
        }

        getLogger().info(getPluginMeta().getDisplayName() + " disabled");
    }

    /** First non-null wins as primary; additional throwables are {@link Throwable#addSuppressed}. */
    private static Throwable mergeThrowables(Throwable primary, Throwable next) {
        if (next == null) {
            return primary;
        }
        if (primary == null) {
            return next;
        }
        primary.addSuppressed(next);
        return primary;
    }

    /**
     * Reloads {@code config.yml}, restarts the ray-trace pool and async tick, reapplies per-world chunk controllers,
     * and re-registers online players so {@link com.vanillage.raytraceantixray.tasks.RayTraceCallable} uses the new settings.
     * Must run on the primary server thread.
     */
    @Override
    public void reloadPluginConfiguration() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("reloadPluginConfiguration must be called from the server thread");
        }

        reloadConfig();
        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);

        stopRayTraceSchedulerForReload();
        startRayTraceSchedulerFromConfig(config);

        for (World w : Bukkit.getWorlds()) {
            WorldListener.handleLoad(this, w);
        }

        PlayerListener.unregisterAndReregisterAll(this);
    }

    private void startRayTraceSchedulerFromConfig(FileConfiguration config) {
        executorService = Executors.newFixedThreadPool(Math.max(config.getInt("settings.anti-xray.ray-trace-threads"), 1), new ThreadFactoryBuilder().setThreadFactory(Executors.defaultThreadFactory()).setNameFormat("RayTraceAntiXray ray trace thread %d").setDaemon(true).build());
        long periodMs = Math.max(config.getLong("settings.anti-xray.ms-per-ray-trace-tick"), 1L);
        rayTraceScheduledTask = Bukkit.getAsyncScheduler().runAtFixedRate(this, new RayTraceTimerTask(this), 0L, periodMs, TimeUnit.MILLISECONDS);
        updateTicks = Math.max(config.getLong("settings.anti-xray.update-ticks"), 1L);
    }

    private void stopRayTraceSchedulerForReload() {
        if (rayTraceScheduledTask != null) {
            rayTraceScheduledTask.cancel();
            rayTraceScheduledTask = null;
        }

        if (executorService != null) {
            executorService.shutdown();

            try {
                if (!executorService.awaitTermination(5L, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();

                    if (!executorService.awaitTermination(1L, TimeUnit.SECONDS)) {
                        getLogger().warning("Ray trace thread pool did not terminate cleanly after reload");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executorService.shutdownNow();
            }
        }
    }

    public boolean isFolia() {
        return folia;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isTimingsEnabled() {
        return timingsEnabled;
    }

    @Override
    public void setTimingsEnabled(boolean timingsEnabled) {
        this.timingsEnabled = timingsEnabled;
    }

    public void enqueuePendingChunkBlocks(UUID playerId, int chunkX, int chunkZ, ChunkBlocks chunkBlocks) {
        long chunkKey = NmsCompat.chunkKey(chunkX, chunkZ);
        pendingChunkBlocksByPlayer
            .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(chunkKey, k -> new ConcurrentLinkedQueue<>())
            .add(chunkBlocks);
    }

    public ChunkBlocks pollPendingChunkBlocks(UUID playerId, int chunkX, int chunkZ) {
        long chunkKey = NmsCompat.chunkKey(chunkX, chunkZ);
        ConcurrentMap<Long, ConcurrentLinkedQueue<ChunkBlocks>> byChunk = pendingChunkBlocksByPlayer.get(playerId);
        if (byChunk == null) {
            return null;
        }
        ConcurrentLinkedQueue<ChunkBlocks> queue = byChunk.get(chunkKey);
        return queue != null ? queue.poll() : null;
    }

    public void clearPendingChunkBlocksFor(UUID playerId) {
        pendingChunkBlocksByPlayer.remove(playerId);
    }

    public ConcurrentMap<UUID, PlayerData> getPlayerData() {
        return playerData;
    }

    /**
     * Replaces {@link PlayerData} while keeping the per-player block-update {@link ScheduledTask}
     * (see {@link PlayerListener}) so disconnect / disable still cancel the correct task.
     */
    public void replacePlayerData(UUID uniqueId, PlayerData newData) {
        PlayerData old = playerData.put(uniqueId, newData);
        if (old != null && old.getBlockUpdateTask() != null) {
            newData.setBlockUpdateTask(old.getBlockUpdateTask());
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public long getUpdateTicks() {
        return updateTicks;
    }

    public boolean isEnabled(World world) {
        AntiXray antiXray = ((CraftWorld) world).getHandle().paperConfig().anticheat.antiXray;

        if (antiXray.enabled && antiXray.engineMode == EngineMode.HIDE) {
            FileConfiguration config = getConfig();
            return config.getBoolean("world-settings." + world.getName() + ".anti-xray.ray-trace", config.getBoolean("world-settings.default.anti-xray.ray-trace"));
        }

        return false;
    }

    public boolean validatePlayer(Player player) {
        return !player.hasMetadata("NPC");
    }

    public boolean validatePlayerData(Player player, PlayerData playerData, String methodName) {
        if (playerData == null) {
            if (validatePlayer(player)) {
                Logger logger = getLogger();
                logger.warning("Missing player data detected for player " + player.getName() + " in method " + methodName);
                logger.warning("If you recently reloaded config, try reconnecting players or restarting the server");
                logger.warning("Also make sure you are using the correct plugin version for your Minecraft version");
            }

            return false;
        }

        return true;
    }

    public static VectorialLocation[] getLocations(Entity entity, VectorialLocation location) {
        World world = location.getWorld();
        ChunkPacketBlockController chunkPacketBlockController = ((CraftWorld) world).getHandle().chunkPacketBlockController;

        if (chunkPacketBlockController instanceof ChunkPacketBlockControllerAntiXray && ((ChunkPacketBlockControllerAntiXray) chunkPacketBlockController).rayTraceThirdPerson) {
            VectorialLocation thirdPersonFrontLocation = new VectorialLocation(location);
            thirdPersonFrontLocation.getDirection().multiply(-1.);
            return new VectorialLocation[] { location, move(entity, new VectorialLocation(world, location.getVector().clone(), location.getDirection())), move(entity, thirdPersonFrontLocation) };
        }

        return new VectorialLocation[] { location };
    }

    private static VectorialLocation move(Entity entity, VectorialLocation location) {
        location.getVector().subtract(location.getDirection().clone().multiply(getMaxZoom(entity, location, 4.)));
        return location;
    }

    private static double getMaxZoom(Entity entity, VectorialLocation location, double maxZoom) {
        Vector vector = location.getVector();
        Vec3 position = new Vec3(vector.getX(), vector.getY(), vector.getZ());
        double positionX = position.x;
        double positionY = position.y;
        double positionZ = position.z;
        Vector direction = location.getDirection();
        double directionX = direction.getX();
        double directionY = direction.getY();
        double directionZ = direction.getZ();
        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
        net.minecraft.world.entity.Entity handle = ((CraftEntity) entity).getHandle();

        // Logic copied from Minecraft client.
        for (int i = 0; i < 8; i++) {
            float cornerX = (float) ((i & 1) * 2 - 1);
            float cornerY = (float) ((i >> 1 & 1) * 2 - 1);
            float cornerZ = (float) ((i >> 2 & 1) * 2 - 1);
            cornerX *= 0.1f;
            cornerY *= 0.1f;
            cornerZ *= 0.1f;
            Vec3 corner = position.add(cornerX, cornerY, cornerZ);
            Vec3 cornerMoved = new Vec3(positionX - directionX * maxZoom + (double) cornerX, positionY - directionY * maxZoom + (double) cornerY, positionZ - directionZ * maxZoom + (double) cornerZ);
            BlockHitResult result = serverLevel.clip(new ClipContext(corner, cornerMoved, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, handle));

            if (result.getType() != HitResult.Type.MISS) {
                double zoom = result.getLocation().distanceTo(position);

                if (zoom < maxZoom) {
                    maxZoom = zoom;
                }
            }
        }

        return maxZoom;
    }
}

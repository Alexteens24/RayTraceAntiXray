package com.vanillage.raytraceantixray.listeners;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

import com.vanillage.raytraceantixray.RayTraceAntiXray;
import com.vanillage.raytraceantixray.antixray.ChunkPacketBlockControllerAntiXray;
import com.vanillage.raytraceantixray.nms.NmsCompat;

import io.papermc.paper.antixray.ChunkPacketBlockController;
import io.papermc.paper.configuration.WorldConfiguration.Anticheat.AntiXray;
import io.papermc.paper.configuration.type.EngineMode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class WorldListener implements Listener {
    private final RayTraceAntiXray plugin;

    public WorldListener(RayTraceAntiXray plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        handleLoad(plugin, event.getWorld());
    }


    public static void handleLoad(RayTraceAntiXray plugin, World world) {
        FileConfiguration config = plugin.getConfig();
        String worldName = world.getName();
        ServerLevel serverLevel = ((CraftWorld) world).getHandle();

        ChunkPacketBlockController controller;

        if (paperUsesEngineModeHide(serverLevel)) {
            if (plugin.isEnabled(world)) {
                boolean rayTraceThirdPerson = config.getBoolean("world-settings." + worldName + ".anti-xray.ray-trace-third-person", config.getBoolean("world-settings.default.anti-xray.ray-trace-third-person"));
                double rayTraceDistance = Math.max(config.getDouble("world-settings." + worldName + ".anti-xray.ray-trace-distance", config.getDouble("world-settings.default.anti-xray.ray-trace-distance")), 0.);
                boolean rehideBlocks = config.getBoolean("world-settings." + worldName + ".anti-xray.rehide-blocks", config.getBoolean("world-settings.default.anti-xray.rehide-blocks"));
                double rehideDistance = Math.max(config.getDouble("world-settings." + worldName + ".anti-xray.rehide-distance", config.getDouble("world-settings.default.anti-xray.rehide-distance")), 0.);
                boolean sectionLeap = config.getBoolean("world-settings." + worldName + ".anti-xray.section-leap", config.getBoolean("world-settings.default.anti-xray.section-leap", false));
                int maxRayTraceBlockCountPerChunk = Math.max(config.getInt("world-settings." + worldName + ".anti-xray.max-ray-trace-block-count-per-chunk", config.getInt("world-settings.default.anti-xray.max-ray-trace-block-count-per-chunk")), 0);
                List<String> rayTraceBlocks = config.getList("world-settings." + worldName + ".anti-xray.ray-trace-blocks", config.getList("world-settings.default.anti-xray.ray-trace-blocks")).stream().filter(Objects::nonNull).map(String::valueOf).collect(Collectors.toList());
                controller = new ChunkPacketBlockControllerAntiXray(plugin, rayTraceThirdPerson, rayTraceDistance, rehideBlocks, rehideDistance, sectionLeap, maxRayTraceBlockCountPerChunk, rayTraceBlocks.isEmpty() ? null : rayTraceBlocks, serverLevel, NmsCompat.serverExecutor(MinecraftServer.getServer()));
            } else {
                controller = new io.papermc.paper.antixray.ChunkPacketBlockControllerAntiXray(serverLevel, NmsCompat.serverExecutor(MinecraftServer.getServer()));
            }
        } else {
            controller = ChunkPacketBlockController.NO_OPERATION_INSTANCE;
        }

        setChunkPacketBlockController(serverLevel, controller);
    }

    private static boolean paperUsesEngineModeHide(ServerLevel serverLevel) {
        AntiXray antiXray = serverLevel.paperConfig().anticheat.antiXray;
        return antiXray.enabled && antiXray.engineMode == EngineMode.HIDE;
    }

    private static void setChunkPacketBlockController(ServerLevel serverLevel, ChunkPacketBlockController controller) {
        try {
            Field field = Level.class.getDeclaredField("chunkPacketBlockController");
            field.setAccessible(true);
            field.set(serverLevel, controller);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

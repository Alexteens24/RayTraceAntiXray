package com.vanillage.raytraceantixray.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import com.vanillage.raytraceantixray.RayTraceAntiXray;
import com.vanillage.raytraceantixray.data.PlayerData;
import com.vanillage.raytraceantixray.data.VectorialLocation;
import com.vanillage.raytraceantixray.tasks.RayTraceCallable;
import com.vanillage.raytraceantixray.tasks.UpdateBukkitRunnable;

public final class PlayerListener implements Listener {
    private final RayTraceAntiXray plugin;

    public PlayerListener(RayTraceAntiXray plugin) {
        this.plugin = plugin;
    }

    /**
     * Players already online when the plugin enables do not get {@link PlayerJoinEvent}.
     */
    public static void registerExistingPlayers(RayTraceAntiXray plugin) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            register(plugin, player);
        }
    }

    /**
     * Clears player state and pending chunk queues, then registers every online player again (e.g. after config reload).
     */
    public static void unregisterAndReregisterAll(RayTraceAntiXray plugin) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();
            PlayerData data = plugin.getPlayerData().get(id);

            if (data != null && data.getBlockUpdateTask() != null) {
                data.getBlockUpdateTask().cancel();
            }

            plugin.clearPendingChunkBlocksFor(id);
            plugin.getPlayerData().remove(id);
        }

        registerExistingPlayers(plugin);
    }

    /**
     * Registers ray-trace data and the repeating block-update task on this player's region scheduler
     * (required on Folia/Canvas so {@link net.minecraft.world.level.Level#getBlockState} has region context).
     */
    public static void register(RayTraceAntiXray plugin, Player player) {
        if (!plugin.validatePlayer(player)) {
            return;
        }

        if (plugin.getPlayerData().containsKey(player.getUniqueId())) {
            return;
        }

        plugin.registerPendingChunkBlocksFor(player.getUniqueId());
        PlayerData playerData = new PlayerData(RayTraceAntiXray.getLocations(player, new VectorialLocation(player.getEyeLocation())));
        playerData.setCallable(new RayTraceCallable(plugin, playerData));
        plugin.getPlayerData().put(player.getUniqueId(), playerData);

        ScheduledTask updateTask = player.getScheduler().runAtFixedRate(plugin, new UpdateBukkitRunnable(plugin, player), null, 1L, plugin.getUpdateTicks());
        playerData.setBlockUpdateTask(updateTask);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        register(plugin, player);
        player.getScheduler().runDelayed(plugin, task -> plugin.sendPaperAntiXrayReminder(player), null, 40L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        PlayerData data = plugin.getPlayerData().get(id);
        if (data != null && data.getBlockUpdateTask() != null) {
            data.getBlockUpdateTask().cancel();
        }
        plugin.clearPendingChunkBlocksFor(id);
        plugin.getPlayerData().remove(id);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getPlayerData().get(player.getUniqueId());

        if (!plugin.validatePlayerData(player, playerData, "onPlayerMove")) {
            return;
        }

        Location to = event.getTo();

        if (to.getWorld().equals(playerData.getLocations()[0].getWorld())) {
            VectorialLocation location = new VectorialLocation(to);
            Vector vector = location.getVector();
            vector.setY(vector.getY() + player.getEyeHeight());
            playerData.setLocations(RayTraceAntiXray.getLocations(player, location));
        }
    }
}

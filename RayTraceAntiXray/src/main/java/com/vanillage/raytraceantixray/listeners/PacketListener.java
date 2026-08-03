package com.vanillage.raytraceantixray.listeners;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUnloadChunk;
import com.vanillage.raytraceantixray.RayTraceAntiXray;
import com.vanillage.raytraceantixray.data.ChunkBlocks;
import com.vanillage.raytraceantixray.data.PlayerData;
import com.vanillage.raytraceantixray.data.VectorialLocation;
import com.vanillage.raytraceantixray.tasks.RayTraceCallable;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import com.vanillage.raytraceantixray.nms.NmsCompat;
import net.minecraft.world.level.chunk.LevelChunk;


public final class PacketListener extends PacketListenerAbstract {
    private final RayTraceAntiXray plugin;

    public PacketListener(RayTraceAntiXray plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.CHUNK_DATA) {
            onChunkData(event);
        } else if (event.getPacketType() == PacketType.Play.Server.UNLOAD_CHUNK) {
            onUnloadChunk(event);
        } else if (event.getPacketType() == PacketType.Play.Server.RESPAWN) {
            onRespawn(event);
        }
    }

    private void onChunkData(PacketSendEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        WrapperPlayServerChunkData wrapper = new WrapperPlayServerChunkData(event);
        int chunkX = wrapper.getColumn().getX();
        int chunkZ = wrapper.getColumn().getZ();

        player.getScheduler().run(plugin, (ScheduledTask task) -> finishChunkData(player, chunkX, chunkZ), null);
    }


    private void finishChunkData(Player player, int chunkX, int chunkZ) {
        if (!player.isOnline()) {
            return;
        }

        ChunkBlocks chunkBlocks = plugin.pollPendingChunkBlocks(player.getUniqueId(), chunkX, chunkZ);

        if (chunkBlocks == null) {


            Location location = player.getEyeLocation();
            ConcurrentMap<UUID, PlayerData> playerDataMap = plugin.getPlayerData();
            UUID uniqueId = player.getUniqueId();
            PlayerData playerData = playerDataMap.get(uniqueId);

            if (!plugin.validatePlayerData(player, playerData, "onPacketSend")) {
                return;
            }

            if (!location.getWorld().equals(playerData.getLocations()[0].getWorld())) {
                playerData = new PlayerData(RayTraceAntiXray.getLocations(player, new VectorialLocation(location)));
                playerData.setCallable(new RayTraceCallable(plugin, playerData));
                plugin.replacePlayerData(uniqueId, playerData);
            }

            return;
        }

        LevelChunk chunk = chunkBlocks.getChunk();

        if (chunk == null) {
            return;
        }

        org.bukkit.World world = chunk.getLevel().getWorld();
        ConcurrentMap<UUID, PlayerData> playerDataMap = plugin.getPlayerData();
        UUID uniqueId = player.getUniqueId();
        PlayerData playerData = playerDataMap.get(uniqueId);

        if (!plugin.validatePlayerData(player, playerData, "onPacketSend")) {
            return;
        }

        if (!world.equals(playerData.getLocations()[0].getWorld())) {
            Location location = player.getEyeLocation();

            if (!world.equals(location.getWorld())) {
                return;
            }

            playerData = new PlayerData(RayTraceAntiXray.getLocations(player, new VectorialLocation(location)));
            playerData.setCallable(new RayTraceCallable(plugin, playerData));
            plugin.replacePlayerData(uniqueId, playerData);
        }

        chunkBlocks = new ChunkBlocks(chunk, new ConcurrentHashMap<>(chunkBlocks.getBlocks()));
        playerData.addChunk(chunkBlocks);
    }

    private void onUnloadChunk(PacketSendEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        WrapperPlayServerUnloadChunk wrapper = new WrapperPlayServerUnloadChunk(event);
        long chunkKey = NmsCompat.chunkKey(wrapper.getChunkX(), wrapper.getChunkZ());

        player.getScheduler().run(plugin, (ScheduledTask task) -> finishUnloadChunk(player, chunkKey), null);
    }

    private void finishUnloadChunk(Player player, long chunkKey) {
        if (!player.isOnline()) {
            return;
        }

        PlayerData playerData = plugin.getPlayerData().get(player.getUniqueId());

        if (!plugin.validatePlayerData(player, playerData, "onPacketSend")) {
            return;
        }

        playerData.removeChunk(chunkKey);
    }

    private void onRespawn(PacketSendEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        player.getScheduler().run(plugin, (ScheduledTask task) -> finishRespawn(player), null);
    }

    private void finishRespawn(Player player) {
        if (!player.isOnline()) {
            return;
        }

        PlayerData playerData = plugin.getPlayerData().get(player.getUniqueId());

        if (!plugin.validatePlayerData(player, playerData, "onPacketSend")) {
            return;
        }

        playerData.clearChunks();
    }
}

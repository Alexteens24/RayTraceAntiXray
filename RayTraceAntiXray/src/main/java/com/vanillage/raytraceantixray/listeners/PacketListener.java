package com.vanillage.raytraceantixray.listeners;

import java.util.HashMap;
import java.util.UUID;
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
import com.vanillage.raytraceantixray.data.LongWrapper;
import com.vanillage.raytraceantixray.data.PlayerData;
import com.vanillage.raytraceantixray.data.VectorialLocation;
import com.vanillage.raytraceantixray.tasks.RayTraceCallable;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Mirrors outgoing chunk / unload / respawn packets via PacketEvents (see
 * <a href="https://javadocs.packetevents.com/">packetevents javadoc</a>).
 * Chunk anti-xray data is matched using a per-player queue keyed by chunk column,
 * filled when Paper finishes obfuscation (see {@link com.vanillage.raytraceantixray.antixray.ChunkPacketBlockControllerAntiXray}).
 * <p>
 * On Folia, {@link PacketSendEvent} may run on a Netty thread; any use of {@link Player}, worlds, or
 * {@link LevelChunk} is deferred to {@link Player#getScheduler()} so region ownership rules are respected.
 */
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

        if (plugin.isFolia()) {
            player.getScheduler().run(plugin, (ScheduledTask task) -> finishChunkData(player, chunkX, chunkZ), null);
        } else {
            finishChunkData(player, chunkX, chunkZ);
        }
    }

    /**
     * Runs on the correct region thread under Folia; may run on the main thread on Paper.
     */
    private void finishChunkData(Player player, int chunkX, int chunkZ) {
        if (!player.isOnline()) {
            return;
        }

        ChunkBlocks chunkBlocks = plugin.pollPendingChunkBlocks(player.getUniqueId(), chunkX, chunkZ);

        if (chunkBlocks == null) {
            // RayTraceAntiXray is probably not enabled in this world (or other plugins bypass Anti-Xray),
            // or Paper did not run shouldModify before getChunkPacketInfo for this send (see ChunkPacketBlockControllerAntiXray).
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
                playerDataMap.put(uniqueId, playerData);
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
            playerDataMap.put(uniqueId, playerData);
        }

        chunkBlocks = new ChunkBlocks(chunk, new HashMap<>(chunkBlocks.getBlocks()));
        playerData.getChunks().put(chunkBlocks.getKey(), chunkBlocks);
    }

    private void onUnloadChunk(PacketSendEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        WrapperPlayServerUnloadChunk wrapper = new WrapperPlayServerUnloadChunk(event);
        long chunkKey = ChunkPos.asLong(wrapper.getChunkX(), wrapper.getChunkZ());

        if (plugin.isFolia()) {
            player.getScheduler().run(plugin, (ScheduledTask task) -> finishUnloadChunk(player, chunkKey), null);
        } else {
            finishUnloadChunk(player, chunkKey);
        }
    }

    private void finishUnloadChunk(Player player, long chunkKey) {
        if (!player.isOnline()) {
            return;
        }

        PlayerData playerData = plugin.getPlayerData().get(player.getUniqueId());

        if (!plugin.validatePlayerData(player, playerData, "onPacketSend")) {
            return;
        }

        playerData.getChunks().remove(new LongWrapper(chunkKey));
    }

    private void onRespawn(PacketSendEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        if (plugin.isFolia()) {
            player.getScheduler().run(plugin, (ScheduledTask task) -> finishRespawn(player), null);
        } else {
            finishRespawn(player);
        }
    }

    private void finishRespawn(Player player) {
        if (!player.isOnline()) {
            return;
        }

        PlayerData playerData = plugin.getPlayerData().get(player.getUniqueId());

        if (!plugin.validatePlayerData(player, playerData, "onPacketSend")) {
            return;
        }

        playerData.getChunks().clear();
    }
}

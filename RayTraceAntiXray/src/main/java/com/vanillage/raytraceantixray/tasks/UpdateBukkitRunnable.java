package com.vanillage.raytraceantixray.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.vanillage.raytraceantixray.RayTraceAntiXray;
import com.vanillage.raytraceantixray.nms.NmsCompat;
import com.vanillage.raytraceantixray.data.ChunkBlocks;
import com.vanillage.raytraceantixray.data.LongWrapper;
import com.vanillage.raytraceantixray.data.PlayerData;
import com.vanillage.raytraceantixray.data.Result;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class UpdateBukkitRunnable extends BukkitRunnable implements Consumer<ScheduledTask> {
    private final RayTraceAntiXray plugin;
    private final Player player;

    public UpdateBukkitRunnable(RayTraceAntiXray plugin) {
        this(plugin, null);
    }

    public UpdateBukkitRunnable(RayTraceAntiXray plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        if (player == null) {
            plugin.getServer().getOnlinePlayers().forEach(this::update);
        } else {
            update(player);
        }
    }

    @Override
    public void accept(ScheduledTask t) {
        run();
    }

    public void update(Player player) {
        PlayerData playerData = plugin.getPlayerData().get(player.getUniqueId());

        if (!plugin.validatePlayerData(player, playerData, "update")) {
            return;
        }

        World world = playerData.getLocations()[0].getWorld();

        if (!player.getWorld().equals(world)) {
            return;
        }

        ConcurrentMap<LongWrapper, ChunkBlocks> chunks = playerData.getChunks();
        ServerLevel serverLevel = ((CraftWorld) world).getHandle();
        Environment environment = world.getEnvironment();
        Queue<Result> results = playerData.getResults();
        Result result;
        List<Packet<?>> packetsToSend = new ArrayList<>();

        while ((result = results.poll()) != null) {
            ChunkBlocks chunkBlocks = result.getChunkBlocks();

            // Check if the client still has the chunk loaded and if it wasn't resent in the meantime.
            // Note that even if this check passes, the server could have already unloaded or resent the chunk but the corresponding packet is still in the packet queue.
            // Technically the null check isn't necessary but we don't need to send an update packet because the client will unload the chunk.
            if (chunkBlocks.getChunk() == null || chunks.get(chunkBlocks.getKey()) != chunkBlocks) {
                continue;
            }

            BlockPos block = result.getBlock();

            // Similar to the null check above, this check isn't actually necessary.
            // However, we don't need to send an update packet because the client will unload the chunk.
            // Thus we can avoid loading the chunk just for the update packet.
            if (!world.isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
                continue;
            }

            BlockState blockState;
            BlockEntity blockEntity = null;

            if (result.isVisible()) {
                blockState = serverLevel.getBlockState(block);

                if (blockState.hasBlockEntity()) {
                    blockEntity = serverLevel.getBlockEntity(block);
                }
            } else if (environment == Environment.NETHER) {
                blockState = Blocks.NETHERRACK.defaultBlockState();
            } else if (environment == Environment.THE_END) {
                blockState = Blocks.END_STONE.defaultBlockState();
            } else if (block.getY() < 0) {
                blockState = Blocks.DEEPSLATE.defaultBlockState();
            } else {
                blockState = Blocks.STONE.defaultBlockState();
            }

            packetsToSend.add(new ClientboundBlockUpdatePacket(block, blockState));

            if (blockEntity != null) {
                Packet<ClientGamePacketListener> bePacket = blockEntity.getUpdatePacket();

                if (bePacket != null) {
                    packetsToSend.add(bePacket);
                }
            }
        }

        // Send via Connection#send so packets go through the normal outbound pipeline (encoding, debug handlers,
        // compatibility with other plugins). Avoids raw Netty writes that could reorder relative to other outbound traffic.
        sendPacketsViaConnection(player, packetsToSend);
    }

    /**
     * Sends {@link ClientboundBlockUpdatePacket} and optional block-entity packets through
     * {@link ServerGamePacketListenerImpl#send(Packet)}, matching normal server behaviour for plugin interoperability.
     */
    private static void sendPacketsViaConnection(Player player, List<Packet<?>> packets) {
        if (packets.isEmpty()) {
            return;
        }

        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;

        if (connection == null || NmsCompat.isConnectionDisconnected(connection)) {
            return;
        }

        for (Packet<?> packet : packets) {
            connection.send(packet);
        }
    }
}

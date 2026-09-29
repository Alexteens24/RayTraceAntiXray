package com.vanillage.raytraceantixray.antixray;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

/**
 * Shared mutable state of a {@link ChunkPacketInfoAntiXray} instance.
 *
 * <p>Kept out of the per-version NMS subclasses so that the player context, the nearby chunk cache and the
 * obfuscation hand-off are defined once instead of once per supported Paper version.
 */
public final class ChunkPacketInfoAntiXrayState {

    private final ChunkPacketBlockControllerAntiXray controller;
    private final @Nullable ServerPlayer targetPlayer;
    private @Nullable LevelChunk[] nearbyChunks;

    public ChunkPacketInfoAntiXrayState(ChunkPacketBlockControllerAntiXray controller, @Nullable ServerPlayer targetPlayer) {
        this.controller = controller;
        this.targetPlayer = targetPlayer;
    }

    public ChunkPacketBlockControllerAntiXray controller() {
        return controller;
    }

    public @Nullable ServerPlayer targetPlayer() {
        return targetPlayer;
    }

    public @Nullable LevelChunk[] nearbyChunks() {
        return nearbyChunks;
    }

    public void nearbyChunks(@Nullable LevelChunk[] nearbyChunks) {
        this.nearbyChunks = nearbyChunks;
    }
}

package com.vanillage.raytraceantixray.antixray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.papermc.paper.antixray.ChunkPacketInfo;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.junit.jupiter.api.Test;

/**
 * Two Anti-Xray controller hooks changed signature between the supported Paper versions, so the controller
 * declares <em>both</em> overloads and lets Paper dispatch to whichever one its own base class declares:
 *
 * <ul>
 *   <li>{@code getChunkPacketInfo(LevelChunk)} — Paper 26.3</li>
 *   <li>{@code getChunkPacketInfo(ClientboundLevelChunkWithLightPacket, LevelChunk)} — Paper 1.21.11 / 26.1.2 / 26.2</li>
 *   <li>{@code onPlayerLeftClickBlock(Level, ...)} — Paper 26.2 / 26.3</li>
 *   <li>{@code onPlayerLeftClickBlock(ServerPlayerGameMode, ...)} — Paper 1.21.11 / 26.1.2</li>
 * </ul>
 *
 * <p>The overload that does not match the running server is simply never called, so a single universal JAR
 * covers every target. These tests guard the extra overloads so they are not removed as "unused" — dropping
 * one silently disables ray tracing on that version.
 */
class ChunkPacketBlockControllerSignatureTest {

    @Test
    void declaresPaper263ChunkOnlyOverload() throws NoSuchMethodException {
        Method method = ChunkPacketBlockControllerAntiXray.class.getMethod("getChunkPacketInfo", LevelChunk.class);
        assertEquals(ChunkPacketInfo.class, method.getReturnType());
        assertTrue(Modifier.isPublic(method.getModifiers()));
    }

    @Test
    void declaresLegacyChunkPacketOverload() throws NoSuchMethodException {
        Method method = ChunkPacketBlockControllerAntiXray.class.getMethod("getChunkPacketInfo", ClientboundLevelChunkWithLightPacket.class, LevelChunk.class);
        assertEquals(ChunkPacketInfo.class, method.getReturnType());
        assertTrue(Modifier.isPublic(method.getModifiers()));
    }

    @Test
    void declaresPaper26xLevelOverloadOfOnPlayerLeftClickBlock() throws NoSuchMethodException {
        assertDeclared("onPlayerLeftClickBlock", Level.class);
    }

    @Test
    void declaresLegacyGameModeOverloadOfOnPlayerLeftClickBlock() throws NoSuchMethodException {
        assertDeclared("onPlayerLeftClickBlock", ServerPlayerGameMode.class);
    }

    private static void assertDeclared(String name, Class<?> firstParameter) throws NoSuchMethodException {
        Method method = ChunkPacketBlockControllerAntiXray.class.getMethod(name, firstParameter, BlockPos.class,
                ServerboundPlayerActionPacket.Action.class, Direction.class, int.class, int.class);
        assertEquals(void.class, method.getReturnType());
        assertTrue(Modifier.isPublic(method.getModifiers()));
    }

    @Test
    void chunkPacketInfoViewIsAnInterfaceOverThePaperApiType() {
        assertTrue(ChunkPacketInfoAntiXray.class.isInterface());
        assertTrue(Runnable.class.isAssignableFrom(ChunkPacketInfoAntiXray.class));
    }
}

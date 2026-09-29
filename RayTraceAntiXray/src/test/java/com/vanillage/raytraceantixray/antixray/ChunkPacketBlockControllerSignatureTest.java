package com.vanillage.raytraceantixray.antixray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.papermc.paper.antixray.ChunkPacketInfo;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.chunk.LevelChunk;
import org.junit.jupiter.api.Test;

/**
 * Paper 26.3 changed {@code ChunkPacketBlockController#getChunkPacketInfo} to take only the chunk, while
 * 1.21.11 / 26.1.2 / 26.2 still pass the chunk packet as well. The controller declares both overloads so
 * that one universal JAR works on every target; Paper dispatches to whichever signature its own base class
 * declares. These tests guard the legacy overload so it is not removed as "unused".
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
    void chunkPacketInfoViewIsAnInterfaceOverThePaperApiType() {
        assertTrue(ChunkPacketInfoAntiXray.class.isInterface());
        assertTrue(Runnable.class.isAssignableFrom(ChunkPacketInfoAntiXray.class));
    }
}

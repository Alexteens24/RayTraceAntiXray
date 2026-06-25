package com.vanillage.raytraceantixray.nms;

import java.lang.reflect.Method;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

/** Version-specific NMS bindings; implementation is selected at runtime. */
public interface NmsBridge {

    long chunkKey(int chunkX, int chunkZ);

    long chunkPosKey(ChunkPos chunkPos);

    int chunkX(ChunkPos chunkPos);

    int chunkZ(ChunkPos chunkPos);

    boolean isConnectionDisconnected(ServerGamePacketListenerImpl connection);

    static NmsBridge get() {
        return Holder.INSTANCE;
    }

    final class Holder {
        private static final NmsBridge INSTANCE;

        static {
            String minecraftVersion = minecraftVersionFromServerBuildInfo();
            if (minecraftVersion == null) {
                minecraftVersion = Bukkit.getServer().getMinecraftVersion();
            }
            final String underscored = minecraftVersion.replace('.', '_');
            final String className = "com.vanillage.raytraceantixray.nms.paper_"
                + underscored + ".NmsCompat" + underscored;
            try {
                INSTANCE = (NmsBridge) Class.forName(className).getDeclaredConstructors()[0].newInstance();
            } catch (final ReflectiveOperationException e) {
                throw new IllegalStateException(
                    "RayTraceAntiXray does not support Minecraft " + minecraftVersion
                        + " (no NMS bindings at " + className + ")", e);
            }
        }

        private static @Nullable String minecraftVersionFromServerBuildInfo() {
            try {
                final Class<?> cls = Class.forName("io.papermc.paper.ServerBuildInfo");
                final Method method = cls.getMethod("minecraftVersionId");
                final Object instance = cls.getMethod("buildInfo").invoke(null);
                return (String) method.invoke(instance);
            } catch (final Throwable e) {
                return null;
            }
        }
    }
}

package com.vanillage.raytraceantixray;

import java.util.List;

/**
 * Narrow surface used by {@link com.vanillage.raytraceantixray.commands.RayTraceAntiXrayTabExecutor} so tests can supply a fake without mocking {@link RayTraceAntiXray}.
 */
public interface RayTraceAntiXrayCommandTarget {
    void reloadPluginConfiguration();

    void setTimingsEnabled(boolean timingsEnabled);

    List<String> getIncompatiblePaperAntiXrayWorlds();

    boolean isPaperAntiXrayReminderEnabled();

    boolean setPaperAntiXrayReminderEnabled(boolean enabled);
}

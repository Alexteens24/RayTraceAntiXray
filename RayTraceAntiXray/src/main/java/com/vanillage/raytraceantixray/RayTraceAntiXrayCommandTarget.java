package com.vanillage.raytraceantixray;

/**
 * Narrow surface used by {@link com.vanillage.raytraceantixray.commands.RayTraceAntiXrayTabExecutor} so tests can supply a fake without mocking {@link RayTraceAntiXray}.
 */
public interface RayTraceAntiXrayCommandTarget {
    void reloadPluginConfiguration();

    void setTimingsEnabled(boolean timingsEnabled);
}

package com.vanillage.raytraceantixray;

import java.util.List;


public interface RayTraceAntiXrayCommandTarget {
    void reloadPluginConfiguration();

    void setTimingsEnabled(boolean timingsEnabled);

    List<String> getIncompatiblePaperAntiXrayWorlds();

    boolean isPaperAntiXrayReminderEnabled();

    boolean setPaperAntiXrayReminderEnabled(boolean enabled);
}

package com.vanillage.raytraceantixray.metrics;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

/** bStats metrics (https://bstats.org/plugin/bukkit/RayTraceAntiXray/31528). */
public final class RayTraceAntiXrayMetrics {
    private static final int BSTATS_PLUGIN_ID = 31528;

    private RayTraceAntiXrayMetrics() {
    }

    public static void register(JavaPlugin plugin, boolean folia, FileConfiguration config) {
        Metrics metrics = new Metrics(plugin, BSTATS_PLUGIN_ID);

        metrics.addCustomChart(new SimplePie("platform", () -> folia ? "folia" : "paper"));

        boolean sectionLeapDefault = config.getBoolean("world-settings.default.anti-xray.section-leap", false);
        metrics.addCustomChart(new SimplePie("section_leap_default", () -> sectionLeapDefault ? "on" : "off"));

        boolean thirdPersonDefault = config.getBoolean("world-settings.default.anti-xray.ray-trace-third-person", false);
        metrics.addCustomChart(new SimplePie("ray_trace_third_person_default", () -> thirdPersonDefault ? "on" : "off"));

        boolean rehideDefault = config.getBoolean("world-settings.default.anti-xray.rehide-blocks", false);
        metrics.addCustomChart(new SimplePie("rehide_blocks_default", () -> rehideDefault ? "on" : "off"));
    }
}

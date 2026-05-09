package com.vanillage.raytraceantixray.tasks;

import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;

import com.vanillage.raytraceantixray.RayTraceAntiXray;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

/**
 * Runs one ray-trace tick: schedules all player {@link com.vanillage.raytraceantixray.data.PlayerData} callables on the plugin executor pool.
 * Intended to be driven by {@link org.bukkit.Bukkit#getAsyncScheduler()} so it does not use a {@link java.util.Timer} thread.
 */
public final class RayTraceTimerTask implements Consumer<ScheduledTask> {
    private final RayTraceAntiXray plugin;

    public RayTraceTimerTask(RayTraceAntiXray plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(ScheduledTask scheduledTask) {
        boolean timingsEnabled = plugin.isTimingsEnabled();
        long start = timingsEnabled ? System.currentTimeMillis() : 0L;

        try {
            plugin.getExecutorService().invokeAll(plugin.getPlayerData().values());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RejectedExecutionException e) {
            if (plugin.isRunning()) {
                plugin.getLogger().log(Level.WARNING, "Ray trace pool rejected a tick (shutdown or saturated)", e);
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.SEVERE, "Error while scheduling ray trace tasks", t);
        }

        if (timingsEnabled) {
            long stop = System.currentTimeMillis();
            plugin.getLogger().info((stop - start) + "ms per ray trace tick.");
        }
    }
}

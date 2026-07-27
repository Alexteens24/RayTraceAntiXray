package com.vanillage.raytraceantixray.reminder;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ReminderState {
    private static final String DISMISSED_PATH = "paper-antixray-reminder-dismissed";

    private final File file;
    private final Logger logger;
    private volatile boolean enabled;

    public ReminderState(File file, Logger logger) {
        this.file = file;
        this.logger = logger;
        this.enabled = loadEnabled();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public synchronized boolean setEnabled(boolean enabled) {
        YamlConfiguration state = new YamlConfiguration();
        state.set(DISMISSED_PATH, !enabled);
        File parent = file.getParentFile();
        File temporaryFile = new File(parent, file.getName() + ".tmp");

        try {
            Files.createDirectories(parent.toPath());
            state.save(temporaryFile);

            try {
                Files.move(temporaryFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporaryFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            this.enabled = enabled;
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save Paper Anti-Xray reminder state to " + file, e);

            try {
                Files.deleteIfExists(temporaryFile.toPath());
            } catch (IOException ignored) {
                // The original save failure is more useful.
            }

            return false;
        }
    }

    private boolean loadEnabled() {
        if (!file.isFile()) {
            return true;
        }

        YamlConfiguration state = new YamlConfiguration();

        try {
            state.load(file);
            return !state.getBoolean(DISMISSED_PATH, false);
        } catch (IOException | InvalidConfigurationException e) {
            logger.log(Level.WARNING, "Could not read Paper Anti-Xray reminder state from " + file + "; reminders remain enabled", e);
            return true;
        }
    }
}

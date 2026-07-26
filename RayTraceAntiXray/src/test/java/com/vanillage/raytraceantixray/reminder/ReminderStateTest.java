package com.vanillage.raytraceantixray.reminder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ReminderStateTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void defaultsEnabledAndPersistsDismissAcrossInstances() {
        File file = temporaryDirectory.resolve("reminder.yml").toFile();
        ReminderState state = new ReminderState(file, Logger.getAnonymousLogger());

        assertTrue(state.isEnabled());
        assertTrue(state.setEnabled(false));
        assertFalse(state.isEnabled());
        assertFalse(new ReminderState(file, Logger.getAnonymousLogger()).isEnabled());
    }

    @Test
    void enableReversesPersistedDismiss() {
        File file = temporaryDirectory.resolve("reminder.yml").toFile();
        ReminderState state = new ReminderState(file, Logger.getAnonymousLogger());

        assertTrue(state.setEnabled(false));
        assertTrue(state.setEnabled(true));
        assertTrue(new ReminderState(file, Logger.getAnonymousLogger()).isEnabled());
    }

    @Test
    void malformedStateFailsSafeToEnabled() throws IOException {
        File file = temporaryDirectory.resolve("reminder.yml").toFile();
        Files.writeString(file.toPath(), "paper-antixray-reminder-dismissed: [unterminated");

        ReminderState state = new ReminderState(file, Logger.getAnonymousLogger());

        assertTrue(state.isEnabled());
    }
}

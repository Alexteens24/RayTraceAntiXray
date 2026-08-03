package com.vanillage.raytraceantixray.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vanillage.raytraceantixray.RayTraceAntiXrayCommandTarget;

import net.kyori.adventure.text.Component;

@ExtendWith(MockitoExtension.class)
final class RayTraceAntiXrayTabExecutorReloadTest {

    private final FakeCommandTarget fakeTarget = new FakeCommandTarget();

    @Mock
    private CommandSender sender;

    @Mock
    private Command command;

    private RayTraceAntiXrayTabExecutor executor;

    @BeforeEach
    void setUp() {
        fakeTarget.reloadCount = 0;
        fakeTarget.reminderEnabled = true;
        fakeTarget.reminderSaveSucceeds = true;
        fakeTarget.incompatibleWorlds = List.of("world");
        when(command.getName()).thenReturn("raytraceantixray");
        executor = new RayTraceAntiXrayTabExecutor(fakeTarget);
    }

    @Test
    void reload_callsReloadPluginConfiguration_andSendsConfirmation() {
        when(sender.hasPermission("raytraceantixray.command.raytraceantixray.reload")).thenReturn(true);

        boolean result = executor.onCommand(sender, command, "raytraceantixray", new String[] { "reload" });

        assertTrue(result);
        assertTrue(fakeTarget.reloadCount == 1);
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void reload_withoutPermission_doesNotCallReload() {
        when(sender.hasPermission("raytraceantixray.command.raytraceantixray.reload")).thenReturn(false);

        boolean result = executor.onCommand(sender, command, "raytraceantixray", new String[] { "reload" });

        assertTrue(result);
        assertTrue(fakeTarget.reloadCount == 0);
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void reload_withExtraArguments_returnsFalseAndDoesNotReload() {
        when(sender.hasPermission("raytraceantixray.command.raytraceantixray.reload")).thenReturn(true);

        boolean result = executor.onCommand(sender, command, "raytraceantixray", new String[] { "reload", "extra" });

        assertFalse(result);
        assertTrue(fakeTarget.reloadCount == 0);
    }

    @Test
    void tabComplete_firstToken_includesReloadWhenPermitted() {
        when(sender.hasPermission("raytraceantixray.command.raytraceantixray.reload")).thenReturn(true);
        when(sender.hasPermission("raytraceantixray.command.raytraceantixray.timings")).thenReturn(false);

        List<String> completions = executor.onTabComplete(sender, command, "raytraceantixray", new String[] { "" });

        assertTrue(completions.contains("reload"));
    }

    @Test
    void tabComplete_firstToken_excludesReloadWithoutPermission() {
        when(sender.hasPermission("raytraceantixray.command.raytraceantixray.reload")).thenReturn(false);
        when(sender.hasPermission("raytraceantixray.command.raytraceantixray.timings")).thenReturn(false);

        List<String> completions = executor.onTabComplete(sender, command, "raytraceantixray", new String[] { "" });

        assertFalse(completions.contains("reload"));
    }

    @Test
    void tabComplete_partialReload_prefixMatches() {
        when(sender.hasPermission("raytraceantixray.command.raytraceantixray.reload")).thenReturn(true);
        when(sender.hasPermission("raytraceantixray.command.raytraceantixray.timings")).thenReturn(false);

        List<String> completions = executor.onTabComplete(sender, command, "raytraceantixray", new String[] { "relo" });

        assertTrue(completions.contains("reload"));
    }

    @Test
    void reminder_showsCompatibilityMessageWhenPermitted() {
        when(sender.hasPermission("raytraceantixray.reminder")).thenReturn(true);

        boolean result = executor.onCommand(sender, command, "raytraceantixray", new String[] { "reminder" });

        assertTrue(result);
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void reminderDismiss_persistsDisabledState() {
        when(sender.hasPermission("raytraceantixray.reminder")).thenReturn(true);

        boolean result = executor.onCommand(sender, command, "raytraceantixray", new String[] { "reminder", "dismiss" });

        assertTrue(result);
        assertFalse(fakeTarget.reminderEnabled);
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void reminder_withoutPermission_doesNotChangeState() {
        when(sender.hasPermission("raytraceantixray.reminder")).thenReturn(false);

        boolean result = executor.onCommand(sender, command, "raytraceantixray", new String[] { "reminder", "dismiss" });

        assertTrue(result);
        assertTrue(fakeTarget.reminderEnabled);
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void tabComplete_includesReminderActionsWhenPermitted() {
        when(sender.hasPermission("raytraceantixray.reminder")).thenReturn(true);

        List<String> completions = executor.onTabComplete(sender, command, "raytraceantixray", new String[] { "reminder", "" });

        assertTrue(completions.contains("dismiss"));
        assertTrue(completions.contains("enable"));
    }

    @Test
    void reminder_withExtraArguments_returnsFalse() {
        when(sender.hasPermission("raytraceantixray.reminder")).thenReturn(true);

        boolean result = executor.onCommand(sender, command, "raytraceantixray", new String[] { "reminder", "dismiss", "extra" });

        assertFalse(result);
        verify(sender, never()).sendMessage(any(Component.class));
    }

    private static final class FakeCommandTarget implements RayTraceAntiXrayCommandTarget {
        int reloadCount;
        boolean reminderEnabled;
        boolean reminderSaveSucceeds;
        List<String> incompatibleWorlds;

        @Override
        public void reloadPluginConfiguration() {
            reloadCount++;
        }

        @Override
        public void setTimingsEnabled(boolean timingsEnabled) {

        }

        @Override
        public List<String> getIncompatiblePaperAntiXrayWorlds() {
            return incompatibleWorlds;
        }

        @Override
        public boolean isPaperAntiXrayReminderEnabled() {
            return reminderEnabled;
        }

        @Override
        public boolean setPaperAntiXrayReminderEnabled(boolean enabled) {
            if (reminderSaveSucceeds) {
                reminderEnabled = enabled;
            }
            return reminderSaveSucceeds;
        }
    }
}

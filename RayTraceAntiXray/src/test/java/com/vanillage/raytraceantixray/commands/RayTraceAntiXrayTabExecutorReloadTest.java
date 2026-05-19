package com.vanillage.raytraceantixray.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

    private static final class FakeCommandTarget implements RayTraceAntiXrayCommandTarget {
        int reloadCount;

        @Override
        public void reloadPluginConfiguration() {
            reloadCount++;
        }

        @Override
        public void setTimingsEnabled(boolean timingsEnabled) {
            // not used by reload tests
        }
    }
}

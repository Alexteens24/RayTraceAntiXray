package com.vanillage.raytraceantixray.reminder;

import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class PaperAntiXrayReminder {
    public static final String PERMISSION = "raytraceantixray.reminder";
    public static final String SETUP_GUIDE_URL = "https://alexteens24.github.io/RayTraceAntiXray/docs/recommended-configuration#paper-anti-xray-setup";
    private static final String CHAT_DIVIDER = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    private static final String CONSOLE_DIVIDER = "============================================================";
    private static final TextColor BRAND_COLOR = TextColor.color(0x55FFFF);
    private static final TextColor ACCENT_COLOR = TextColor.color(0xFFD166);
    private static final TextColor SUCCESS_COLOR = TextColor.color(0x57F287);
    private static final TextColor MUTED_COLOR = TextColor.color(0xA0A0A0);
    private static final TextColor DIVIDER_COLOR = TextColor.color(0x2F6F78);

    private PaperAntiXrayReminder() {
    }

    public static boolean isIncompatible(boolean rayTraceRequested, boolean paperEnabled, boolean paperUsesEngineModeHide) {
        return rayTraceRequested && (!paperEnabled || !paperUsesEngineModeHide);
    }

    public static Component message(List<String> incompatibleWorlds) {
        String worlds = String.join(", ", incompatibleWorlds);
        Component openGuide = Component.text("▶ OPEN SETUP GUIDE", SUCCESS_COLOR, TextDecoration.BOLD)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(SETUP_GUIDE_URL))
                .hoverEvent(HoverEvent.showText(
                        Component.text("Open the step-by-step setup guide", SUCCESS_COLOR)
                                .append(Component.newline())
                                .append(Component.text(SETUP_GUIDE_URL, NamedTextColor.GRAY))));
        Component dismiss = Component.text("✕ DISMISS", MUTED_COLOR)
                .clickEvent(ClickEvent.runCommand("/raytraceantixray reminder dismiss"))
                .hoverEvent(HoverEvent.showText(
                        Component.text("Hide this reminder for the server", NamedTextColor.GRAY)
                                .append(Component.newline())
                                .append(Component.text("Re-enable: /raytraceantixray reminder enable", MUTED_COLOR))));

        return Component.text(CHAT_DIVIDER, DIVIDER_COLOR)
                .append(Component.newline())
                .append(Component.text("  RayTraceAntiXray", BRAND_COLOR, TextDecoration.BOLD))
                .append(Component.text("  •  SETUP ASSISTANT", MUTED_COLOR))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("  ✓  ", SUCCESS_COLOR, TextDecoration.BOLD))
                .append(Component.text("Plugin loaded successfully", NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("  !  ", ACCENT_COLOR, TextDecoration.BOLD))
                .append(Component.text("Paper Anti-Xray needs ", NamedTextColor.WHITE))
                .append(Component.text("engine-mode: 1", ACCENT_COLOR, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("     Worlds  ", MUTED_COLOR))
                .append(Component.text(worlds, NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("  Update Paper's config, then fully restart the server.", MUTED_COLOR))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("  "))
                .append(openGuide)
                .append(Component.text("     "))
                .append(dismiss)
                .append(Component.newline())
                .append(Component.text(CHAT_DIVIDER, DIVIDER_COLOR));
    }

    public static List<String> consoleMessages(List<String> incompatibleWorlds) {
        String worlds = String.join(", ", incompatibleWorlds);

        return List.of(
                CONSOLE_DIVIDER,
                " RayTraceAntiXray Setup Assistant",
                CONSOLE_DIVIDER,
                " STATUS : Paper Anti-Xray setup required",
                " WORLDS : " + worlds,
                " ACTION : Set engine-mode: 1, then fully restart the server",
                " GUIDE  : " + SETUP_GUIDE_URL,
                " NOTE   : No configuration files were changed automatically",
                CONSOLE_DIVIDER);
    }
}

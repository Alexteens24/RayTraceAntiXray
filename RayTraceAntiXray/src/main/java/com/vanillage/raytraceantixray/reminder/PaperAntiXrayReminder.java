package com.vanillage.raytraceantixray.reminder;

import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class PaperAntiXrayReminder {
    public static final String PERMISSION = "raytraceantixray.reminder";
    public static final String SETUP_GUIDE_URL = "https://alexteens24.github.io/RayTraceAntiXray/docs/recommended-configuration#paper-anti-xray-setup";

    private PaperAntiXrayReminder() {
    }

    public static boolean isIncompatible(boolean rayTraceRequested, boolean paperEnabled, boolean paperUsesEngineModeHide) {
        return rayTraceRequested && (!paperEnabled || !paperUsesEngineModeHide);
    }

    public static Component message(List<String> incompatibleWorlds) {
        String worlds = String.join(", ", incompatibleWorlds);
        Component openGuide = Component.text("[Show me how]", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.openUrl(SETUP_GUIDE_URL))
                .hoverEvent(HoverEvent.showText(Component.text("Open the quick setup guide")));
        Component dismiss = Component.text("[Don't remind me]", NamedTextColor.GRAY)
                .clickEvent(ClickEvent.runCommand("/raytraceantixray reminder dismiss"))
                .hoverEvent(HoverEvent.showText(Component.text("You can turn reminders back on with /raytraceantixray reminder enable")));

        return Component.text("[RayTraceAntiXray] ", NamedTextColor.AQUA)
                .append(Component.text("Just one more setup step!", NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(Component.text("The plugin is ready, but Paper Anti-Xray still needs engine-mode 1 in: ", NamedTextColor.WHITE))
                .append(Component.text(worlds, NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(Component.text("Follow the quick guide, update Paper's config, then fully restart the server. That's it!", NamedTextColor.WHITE))
                .append(Component.newline())
                .append(openGuide)
                .append(Component.space())
                .append(dismiss);
    }

    public static List<String> consoleMessages(List<String> incompatibleWorlds) {
        String worlds = String.join(", ", incompatibleWorlds);

        return List.of(
                "One quick setup step is still needed for RayTraceAntiXray.",
                "Paper Anti-Xray needs engine-mode 1 in: " + worlds,
                "No configuration was changed automatically. Follow the guide, then fully restart the server:",
                SETUP_GUIDE_URL);
    }
}

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
        Component openGuide = Component.text("[Open setup guide]", NamedTextColor.GOLD)
                .clickEvent(ClickEvent.openUrl(SETUP_GUIDE_URL))
                .hoverEvent(HoverEvent.showText(Component.text("Open the Paper Anti-Xray setup guide")));
        Component dismiss = Component.text("[Dismiss for this server]", NamedTextColor.GRAY)
                .clickEvent(ClickEvent.runCommand("/raytraceantixray reminder dismiss"))
                .hoverEvent(HoverEvent.showText(Component.text("Stop showing this reminder until it is enabled again")));

        return Component.text("[RayTraceAntiXray] ", NamedTextColor.AQUA)
                .append(Component.text("Setup required", NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(Component.text("Paper Anti-Xray must be enabled with engine-mode 1 for: ", NamedTextColor.WHITE))
                .append(Component.text(worlds, NamedTextColor.YELLOW))
                .append(Component.text(". A full server restart is required after changing Paper configuration.", NamedTextColor.WHITE))
                .append(Component.newline())
                .append(openGuide)
                .append(Component.space())
                .append(dismiss);
    }
}

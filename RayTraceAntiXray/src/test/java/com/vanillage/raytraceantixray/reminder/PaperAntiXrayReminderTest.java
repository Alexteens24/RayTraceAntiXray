package com.vanillage.raytraceantixray.reminder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

final class PaperAntiXrayReminderTest {

    @Test
    void incompatible_onlyWhenRayTracingRequestedAndPaperIsNotHideMode() {
        assertFalse(PaperAntiXrayReminder.isIncompatible(false, false, false));
        assertTrue(PaperAntiXrayReminder.isIncompatible(true, false, false));
        assertTrue(PaperAntiXrayReminder.isIncompatible(true, true, false));
        assertFalse(PaperAntiXrayReminder.isIncompatible(true, true, true));
    }

    @Test
    void messageContainsSetupUrlAndDismissCommand() {
        Component message = PaperAntiXrayReminder.message(List.of("world", "resource"));
        assertTrue(hasClickEvent(message, ClickEvent.openUrl(PaperAntiXrayReminder.SETUP_GUIDE_URL)));
        assertTrue(hasClickEvent(message, ClickEvent.runCommand("/raytraceantixray reminder dismiss")));
    }

    private static Stream<Component> components(Component component) {
        return Stream.concat(Stream.of(component), component.children().stream().flatMap(PaperAntiXrayReminderTest::components));
    }

    private static boolean hasClickEvent(Component component, ClickEvent<?> expected) {
        return components(component)
                .map(child -> child.style().clickEvent())
                .filter(Objects::nonNull)
                .anyMatch(expected::equals);
    }
}

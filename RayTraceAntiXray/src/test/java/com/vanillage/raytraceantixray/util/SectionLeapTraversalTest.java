package com.vanillage.raytraceantixray.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.vanillage.raytraceantixray.util.BlockOcclusionCulling.BlockOcclusionGetter;
import com.vanillage.raytraceantixray.util.bench.MockSectionLeapGetters;

/**
 * Verifies section-leap traversal matches legacy DDA for the same {@link BlockOcclusionGetter}.
 */
final class SectionLeapTraversalTest {

    private record Scenario(String label, int oreX, int oreY, int oreZ, double eyeX, double eyeY, double eyeZ, BlockOcclusionGetter getter) {
    }

    static Stream<Scenario> scenarios() {
        return Stream.of(
            new Scenario("long +X strip", 0, 8, 8, 128.5, 8.5, 8.5, MockSectionLeapGetters.stripY16Z16()),
            new Scenario("diagonal checkerboard air", 0, 0, 0, 88.5, 88.5, 88.5, MockSectionLeapGetters.checkerboardSections()),
            new Scenario("grazing +X", 0, 8, 8, 220.5, 8.06, 8.06, MockSectionLeapGetters.stripY16Z16()),
            new Scenario("steep climb", 4, 10, 6, 72.5, 90.5, 6.5, MockSectionLeapGetters.evenSectionYLayers()),
            new Scenario("leap disabled path", 0, 8, 8, 100.5, 8.5, 8.5, MockSectionLeapGetters.neverAirSections()),
            new Scenario("solid wall at x=64", 0, 8, 8, 128.5, 8.5, 8.5, MockSectionLeapGetters.wallAtX(64))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void sectionLeap_matchesLegacyVisibility(Scenario scenario) {
        BlockIterator itLegacy = new BlockIterator(0., 0., 0., 0., 0., 0.);
        BlockIterator itLeap = new BlockIterator(0., 0., 0., 0., 0., 0.);
        BlockOcclusionCulling legacy = new BlockOcclusionCulling(itLegacy::initializeNormalized, scenario.getter(), false, false);
        BlockOcclusionCulling leap = new BlockOcclusionCulling(itLeap::initializeNormalized, scenario.getter(), false, true);

        double cx = scenario.oreX() + 0.5;
        double cy = scenario.oreY() + 0.5;
        double cz = scenario.oreZ() + 0.5;
        double diffX = scenario.eyeX() - cx;
        double diffY = scenario.eyeY() - cy;
        double diffZ = scenario.eyeZ() - cz;
        double distSq = diffX * diffX + diffY * diffY + diffZ * diffZ;
        double len = Math.sqrt(distSq);
        double dirX = diffX / len;
        double dirY = diffY / len;
        double dirZ = diffZ / len;

        for (int i = 0; i < 32; i++) {
            boolean legacyVisible = legacy.isVisible(scenario.oreX(), scenario.oreY(), scenario.oreZ(), cx, cy, cz, diffX, diffY, diffZ, distSq, dirX, dirY, dirZ);
            boolean leapVisible = leap.isVisible(scenario.oreX(), scenario.oreY(), scenario.oreZ(), cx, cy, cz, diffX, diffY, diffZ, distSq, dirX, dirY, dirZ);
            assertEquals(legacyVisible, leapVisible, "mismatch on warmup iteration " + i + " for " + scenario.label());
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void sectionLeap_matchesLegacyVisibility_frustumOn(Scenario scenario) {
        BlockIterator itLegacy = new BlockIterator(0., 0., 0., 0., 0., 0.);
        BlockIterator itLeap = new BlockIterator(0., 0., 0., 0., 0., 0.);
        BlockOcclusionCulling legacy = new BlockOcclusionCulling(itLegacy::initializeNormalized, scenario.getter(), true, false);
        BlockOcclusionCulling leap = new BlockOcclusionCulling(itLeap::initializeNormalized, scenario.getter(), true, true);

        double cx = scenario.oreX() + 0.5;
        double cy = scenario.oreY() + 0.5;
        double cz = scenario.oreZ() + 0.5;
        double diffX = scenario.eyeX() - cx;
        double diffY = scenario.eyeY() - cy;
        double diffZ = scenario.eyeZ() - cz;
        double distSq = diffX * diffX + diffY * diffY + diffZ * diffZ;
        double len = Math.sqrt(distSq);
        double dirX = diffX / len;
        double dirY = diffY / len;
        double dirZ = diffZ / len;

        assertEquals(
            legacy.isVisible(scenario.oreX(), scenario.oreY(), scenario.oreZ(), cx, cy, cz, diffX, diffY, diffZ, distSq, dirX, dirY, dirZ),
            leap.isVisible(scenario.oreX(), scenario.oreY(), scenario.oreZ(), cx, cy, cz, diffX, diffY, diffZ, distSq, dirX, dirY, dirZ));
    }

    @Test
    void gridManyShortRays_spotCheckAgreement() {
        BlockOcclusionGetter getter = MockSectionLeapGetters.stripY16Z16();
        BlockIterator itLegacy = new BlockIterator(0., 0., 0., 0., 0., 0.);
        BlockIterator itLeap = new BlockIterator(0., 0., 0., 0., 0., 0.);
        BlockOcclusionCulling legacy = new BlockOcclusionCulling(itLegacy::initializeNormalized, getter, false, false);
        BlockOcclusionCulling leap = new BlockOcclusionCulling(itLeap::initializeNormalized, getter, false, true);

        double eyeX = 96.5;
        double eyeY = 10.5;
        double eyeZ = 10.5;
        int gridSize = 128;

        for (int k = 0; k < gridSize; k++) {
            int oreX = k & 15;
            int oreY = 4 + ((k >> 2) & 7);
            int oreZ = 4 + ((k >> 5) & 7);
            assertEquals(rayOnce(legacy, oreX, oreY, oreZ, eyeX, eyeY, eyeZ), rayOnce(leap, oreX, oreY, oreZ, eyeX, eyeY, eyeZ), "grid index " + k);
        }
    }

    @Test
    void wallScenario_leapAndLegacyBothOccluded() {
        Scenario scenario = new Scenario("wall", 0, 8, 8, 128.5, 8.5, 8.5, MockSectionLeapGetters.wallAtX(64));
        BlockIterator itLegacy = new BlockIterator(0., 0., 0., 0., 0., 0.);
        BlockIterator itLeap = new BlockIterator(0., 0., 0., 0., 0., 0.);
        BlockOcclusionCulling legacy = new BlockOcclusionCulling(itLegacy::initializeNormalized, scenario.getter(), false, false);
        BlockOcclusionCulling leap = new BlockOcclusionCulling(itLeap::initializeNormalized, scenario.getter(), false, true);

        double cx = 0.5;
        double cy = 8.5;
        double cz = 8.5;
        double diffX = 128.;
        double diffY = 0.;
        double diffZ = 0.;
        double distSq = diffX * diffX;
        double len = Math.sqrt(distSq);

        assertFalse(legacy.isVisible(0, 8, 8, cx, cy, cz, diffX, diffY, diffZ, distSq, diffX / len, diffY / len, diffZ / len));
        assertFalse(leap.isVisible(0, 8, 8, cx, cy, cz, diffX, diffY, diffZ, distSq, diffX / len, diffY / len, diffZ / len));
    }

    private static boolean rayOnce(BlockOcclusionCulling c, int oreX, int oreY, int oreZ, double eyeX, double eyeY, double eyeZ) {
        double cx = oreX + 0.5;
        double cy = oreY + 0.5;
        double cz = oreZ + 0.5;
        double diffX = eyeX - cx;
        double diffY = eyeY - cy;
        double diffZ = eyeZ - cz;
        double distSq = diffX * diffX + diffY * diffY + diffZ * diffZ;
        double len = Math.sqrt(distSq);
        return c.isVisible(oreX, oreY, oreZ, cx, cy, cz, diffX, diffY, diffZ, distSq, diffX / len, diffY / len, diffZ / len);
    }

}

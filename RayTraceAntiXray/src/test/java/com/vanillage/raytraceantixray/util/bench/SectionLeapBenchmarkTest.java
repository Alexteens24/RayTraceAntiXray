package com.vanillage.raytraceantixray.util.bench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.vanillage.raytraceantixray.util.BlockIterator;
import com.vanillage.raytraceantixray.util.BlockOcclusionCulling;
import com.vanillage.raytraceantixray.util.BlockOcclusionCulling.BlockOcclusionGetter;


@Tag("bench")
final class SectionLeapBenchmarkTest {

    private record Scenario(String label, int oreX, int oreY, int oreZ, double eyeX, double eyeY, double eyeZ, BlockOcclusionGetter getter) {
    }

    private record Timings(double msDdaOnly, double msSectionLeap, boolean resultsMatch) {
        double speedup() {
            return msSectionLeap > 0. ? msDdaOnly / msSectionLeap : 0.;
        }
    }

    @Test
    void compareDdaOnlyVsSectionLeap_printTable() {
        List<Scenario> scenarios = new ArrayList<>();
        scenarios.add(new Scenario("A: long +X, air strip y,z<16", 0, 8, 8, 128.5, 8.5, 8.5, MockSectionLeapGetters.stripY16Z16()));
        scenarios.add(new Scenario("B: diagonal, checkerboard air sections", 0, 0, 0, 88.5, 88.5, 88.5, MockSectionLeapGetters.checkerboardSections()));
        scenarios.add(new Scenario("C: grazing +X", 0, 8, 8, 220.5, 8.06, 8.06, MockSectionLeapGetters.stripY16Z16()));
        scenarios.add(new Scenario("D: steep +Y, even sectionY air", 4, 10, 6, 72.5, 90.5, 6.5, MockSectionLeapGetters.evenSectionYLayers()));
        scenarios.add(new Scenario("E: never air (leap off path)", 0, 8, 8, 100.5, 8.5, 8.5, MockSectionLeapGetters.neverAirSections()));
        scenarios.add(new Scenario("F: wall at x=64", 0, 8, 8, 128.5, 8.5, 8.5, MockSectionLeapGetters.wallAtX(64)));

        int warmup = 2_000;
        int reps = 15_000;

        System.out.println();
        System.out.println("========== SectionLeapBenchmark (DDA only vs section-leap) ==========");
        System.out.println("section-leap false = per-voxel DDA (config: section-leap: false)");
        System.out.println("section-leap true  = skip air-only 16³ sections");
        System.out.printf("warmup=%d  reps=%d  frustum=off%n", warmup, reps);
        System.out.printf("%-48s %12s %12s %10s %6s%n", "Scenario", "DDA ms", "leap ms", "speedup", "match");
        System.out.println("-".repeat(94));

        for (Scenario scenario : scenarios) {
            Timings t = runScenario(scenario, warmup, reps);
            System.out.printf("%-48s %12.2f %12.2f %9.2fx %6s%n",
                truncate(scenario.label(), 48),
                t.msDdaOnly(),
                t.msSectionLeap(),
                t.speedup(),
                t.resultsMatch() ? "yes" : "NO");
            assertTrue(t.resultsMatch(), "visibility mismatch: " + scenario.label());
        }

        Timings grid = runGridBatch(MockSectionLeapGetters.stripY16Z16(), 500, 128, 24);
        System.out.println("-".repeat(94));
        System.out.printf("%-48s %12.2f %12.2f %9.2fx %6s%n",
            truncate("G: 128 rays x 24 batches (strip getter)", 48),
            grid.msDdaOnly(),
            grid.msSectionLeap(),
            grid.speedup(),
            grid.resultsMatch() ? "yes" : "NO");
        assertTrue(grid.resultsMatch());

        System.out.println("=====================================================================");
        System.out.println();
    }

    private static Timings runScenario(Scenario scenario, int warmup, int reps) {
        BlockIterator itDda = new BlockIterator(0., 0., 0., 0., 0., 0.);
        BlockIterator itLeap = new BlockIterator(0., 0., 0., 0., 0., 0.);
        BlockOcclusionCulling ddaOnly = new BlockOcclusionCulling(itDda::initializeNormalized, scenario.getter(), false, false);
        BlockOcclusionCulling sectionLeap = new BlockOcclusionCulling(itLeap::initializeNormalized, scenario.getter(), false, true);

        RayVectors v = RayVectors.from(scenario);

        for (int i = 0; i < warmup; i++) {
            ddaOnly.isVisible(scenario.oreX(), scenario.oreY(), scenario.oreZ(), v.cx(), v.cy(), v.cz(), v.diffX(), v.diffY(), v.diffZ(), v.distSq(), v.dirX(), v.dirY(), v.dirZ());
            sectionLeap.isVisible(scenario.oreX(), scenario.oreY(), scenario.oreZ(), v.cx(), v.cy(), v.cz(), v.diffX(), v.diffY(), v.diffZ(), v.distSq(), v.dirX(), v.dirY(), v.dirZ());
        }

        long t0 = System.nanoTime();
        for (int i = 0; i < reps; i++) {
            ddaOnly.isVisible(scenario.oreX(), scenario.oreY(), scenario.oreZ(), v.cx(), v.cy(), v.cz(), v.diffX(), v.diffY(), v.diffZ(), v.distSq(), v.dirX(), v.dirY(), v.dirZ());
        }
        long t1 = System.nanoTime();
        for (int i = 0; i < reps; i++) {
            sectionLeap.isVisible(scenario.oreX(), scenario.oreY(), scenario.oreZ(), v.cx(), v.cy(), v.cz(), v.diffX(), v.diffY(), v.diffZ(), v.distSq(), v.dirX(), v.dirY(), v.dirZ());
        }
        long t2 = System.nanoTime();

        boolean ddaResult = ddaOnly.isVisible(scenario.oreX(), scenario.oreY(), scenario.oreZ(), v.cx(), v.cy(), v.cz(), v.diffX(), v.diffY(), v.diffZ(), v.distSq(), v.dirX(), v.dirY(), v.dirZ());
        boolean leapResult = sectionLeap.isVisible(scenario.oreX(), scenario.oreY(), scenario.oreZ(), v.cx(), v.cy(), v.cz(), v.diffX(), v.diffY(), v.diffZ(), v.distSq(), v.dirX(), v.dirY(), v.dirZ());
        assertEquals(ddaResult, leapResult);

        return new Timings((t1 - t0) / 1_000_000.0, (t2 - t1) / 1_000_000.0, ddaResult == leapResult);
    }

    private static Timings runGridBatch(BlockOcclusionGetter getter, int warmup, int gridSize, int batchRepeats) {
        BlockIterator itDda = new BlockIterator(0., 0., 0., 0., 0., 0.);
        BlockIterator itLeap = new BlockIterator(0., 0., 0., 0., 0., 0.);
        BlockOcclusionCulling ddaOnly = new BlockOcclusionCulling(itDda::initializeNormalized, getter, false, false);
        BlockOcclusionCulling sectionLeap = new BlockOcclusionCulling(itLeap::initializeNormalized, getter, false, true);

        double eyeX = 96.5;
        double eyeY = 10.5;
        double eyeZ = 10.5;

        for (int w = 0; w < warmup; w++) {
            int k = w % gridSize;
            rayOnce(ddaOnly, k & 15, 4 + ((k >> 2) & 7), 4 + ((k >> 5) & 7), eyeX, eyeY, eyeZ);
            rayOnce(sectionLeap, k & 15, 4 + ((k >> 2) & 7), 4 + ((k >> 5) & 7), eyeX, eyeY, eyeZ);
        }

        long t0 = System.nanoTime();
        for (int r = 0; r < batchRepeats; r++) {
            for (int k = 0; k < gridSize; k++) {
                rayOnce(ddaOnly, k & 15, 4 + ((k >> 2) & 7), 4 + ((k >> 5) & 7), eyeX, eyeY, eyeZ);
            }
        }
        long t1 = System.nanoTime();
        for (int r = 0; r < batchRepeats; r++) {
            for (int k = 0; k < gridSize; k++) {
                rayOnce(sectionLeap, k & 15, 4 + ((k >> 2) & 7), 4 + ((k >> 5) & 7), eyeX, eyeY, eyeZ);
            }
        }
        long t2 = System.nanoTime();

        int spotChecks = 64;
        int spotOk = 0;
        for (int j = 0; j < spotChecks; j++) {
            int k = (j * gridSize) / spotChecks;
            int oreX = k & 15;
            int oreY = 4 + ((k >> 2) & 7);
            int oreZ = 4 + ((k >> 5) & 7);
            if (rayOnce(ddaOnly, oreX, oreY, oreZ, eyeX, eyeY, eyeZ) == rayOnce(sectionLeap, oreX, oreY, oreZ, eyeX, eyeY, eyeZ)) {
                spotOk++;
            }
        }

        return new Timings((t1 - t0) / 1_000_000.0, (t2 - t1) / 1_000_000.0, spotOk == spotChecks);
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

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private record RayVectors(double cx, double cy, double cz, double diffX, double diffY, double diffZ, double distSq, double dirX, double dirY, double dirZ) {
        static RayVectors from(Scenario s) {
            double cx = s.oreX() + 0.5;
            double cy = s.oreY() + 0.5;
            double cz = s.oreZ() + 0.5;
            double diffX = s.eyeX() - cx;
            double diffY = s.eyeY() - cy;
            double diffZ = s.eyeZ() - cz;
            double distSq = diffX * diffX + diffY * diffY + diffZ * diffZ;
            double len = Math.sqrt(distSq);
            return new RayVectors(cx, cy, cz, diffX, diffY, diffZ, distSq, diffX / len, diffY / len, diffZ / len);
        }
    }
}

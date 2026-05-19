package com.vanillage.raytraceantixray.util.bench;

import com.vanillage.raytraceantixray.util.BlockOcclusionCulling.BlockOcclusionGetter;

/** Mock {@link BlockOcclusionGetter} implementations for section-leap tests and benchmarks. */
public final class MockSectionLeapGetters {
    private MockSectionLeapGetters() {
    }

    public static BlockOcclusionGetter stripY16Z16() {
        return new BlockOcclusionGetter() {
            @Override
            public boolean isOccluding(int x, int y, int z) {
                return false;
            }

            @Override
            public boolean sectionHasOnlyAir(int bx, int by, int bz) {
                return by >= 0 && by < 16 && bz >= 0 && bz < 16;
            }
        };
    }

    public static BlockOcclusionGetter checkerboardSections() {
        return new BlockOcclusionGetter() {
            @Override
            public boolean isOccluding(int x, int y, int z) {
                return false;
            }

            @Override
            public boolean sectionHasOnlyAir(int bx, int by, int bz) {
                int sx = bx >> 4;
                int sy = by >> 4;
                int sz = bz >> 4;
                return by >= 0 && by < 128 && bz >= 0 && bz < 128 && (((sx + sy + sz) & 1) == 0);
            }
        };
    }

    public static BlockOcclusionGetter evenSectionYLayers() {
        return new BlockOcclusionGetter() {
            @Override
            public boolean isOccluding(int x, int y, int z) {
                return false;
            }

            @Override
            public boolean sectionHasOnlyAir(int bx, int by, int bz) {
                int sy = by >> 4;
                return (sy & 1) == 0 && bz >= 0 && bz < 16;
            }
        };
    }

    /** {@code sectionHasOnlyAir} always false — leap path never taken; DDA vs leap should match. */
    public static BlockOcclusionGetter neverAirSections() {
        return new BlockOcclusionGetter() {
            @Override
            public boolean isOccluding(int x, int y, int z) {
                return false;
            }
        };
    }

    public static BlockOcclusionGetter wallAtX(int wallX) {
        return new BlockOcclusionGetter() {
            @Override
            public boolean isOccluding(int x, int y, int z) {
                return x == wallX;
            }

            @Override
            public boolean sectionHasOnlyAir(int bx, int by, int bz) {
                return by >= 0 && by < 16 && bz >= 0 && bz < 16 && (bx >> 4) != (wallX >> 4);
            }
        };
    }
}

package com.vanillage.raytraceantixray.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class SectionRayMathTest {

    @Test
    void sectionExitParameter_axisAlignedPositiveX() {
        double t = SectionRayMath.sectionExitParameter(0.5, 8.5, 8.5, 1., 0., 0., 0, 8, 8);
        assertEquals(15.5, t, 1e-9);
    }

    @Test
    void sectionExitParameter_axisAlignedNegativeX() {
        double t = SectionRayMath.sectionExitParameter(31.5, 8.5, 8.5, -1., 0., 0., 31, 8, 8);
        assertEquals(15.5, t, 1e-9);
    }

    @Test
    void sectionExitParameter_parallelRayOutsideSlab_returnsNaN() {

        assertFalse(Double.isFinite(SectionRayMath.sectionExitParameter(0.5, 32.5, 8.5, 1., 0., 0., 0, 8, 8)));
    }

    @Test
    void sectionExitParameter_diagonalThroughSection() {
        double invSqrt3 = 1.0 / Math.sqrt(3.0);
        double t = SectionRayMath.sectionExitParameter(0.5, 0.5, 0.5, invSqrt3, invSqrt3, invSqrt3, 0, 0, 0);
        assertTrue(t > 0.);
        assertTrue(t < 30.);
    }
}

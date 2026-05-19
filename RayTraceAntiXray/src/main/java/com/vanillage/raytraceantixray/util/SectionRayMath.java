package com.vanillage.raytraceantixray.util;

/**
 * Ray vs Minecraft chunk-section AABB (16×16×16 block cells) for empty-space skipping.
 */
public final class SectionRayMath {
    private static final double EPS = 1e-9;

    private SectionRayMath() {
    }

    /**
     * @return ray parameter {@code t} along {@code P(t) = origin + t * dir} (unit {@code dir}) to the
     *         <strong>exit</strong> of the axis-aligned box of the section containing {@code (bx, by, bz)},
     *         or {@link Double#NaN} if the ray misses the section interval.
     */
    public static double sectionExitParameter(double ox, double oy, double oz, double dx, double dy, double dz, int bx, int by, int bz) {
        int sx = bx >> 4;
        int sy = by >> 4;
        int sz = bz >> 4;
        double minX = sx * 16.0;
        double minY = sy * 16.0;
        double minZ = sz * 16.0;
        double maxX = minX + 16.0;
        double maxY = minY + 16.0;
        double maxZ = minZ + 16.0;
        double tMin = 0.0;
        double tMax = Double.POSITIVE_INFINITY;

        if (Math.abs(dx) < EPS) {
            if (ox < minX - EPS || ox > maxX + EPS) {
                return Double.NaN;
            }
        } else {
            double invD = 1.0 / dx;
            double tNear = (minX - ox) * invD;
            double tFar = (maxX - ox) * invD;

            if (tNear > tFar) {
                double s = tNear;
                tNear = tFar;
                tFar = s;
            }

            tMin = Math.max(tMin, tNear);
            tMax = Math.min(tMax, tFar);
        }

        if (Math.abs(dy) < EPS) {
            if (oy < minY - EPS || oy > maxY + EPS) {
                return Double.NaN;
            }
        } else {
            double invD = 1.0 / dy;
            double tNear = (minY - oy) * invD;
            double tFar = (maxY - oy) * invD;

            if (tNear > tFar) {
                double s = tNear;
                tNear = tFar;
                tFar = s;
            }

            tMin = Math.max(tMin, tNear);
            tMax = Math.min(tMax, tFar);
        }

        if (Math.abs(dz) < EPS) {
            if (oz < minZ - EPS || oz > maxZ + EPS) {
                return Double.NaN;
            }
        } else {
            double invD = 1.0 / dz;
            double tNear = (minZ - oz) * invD;
            double tFar = (maxZ - oz) * invD;

            if (tNear > tFar) {
                double s = tNear;
                tNear = tFar;
                tFar = s;
            }

            tMin = Math.max(tMin, tNear);
            tMax = Math.min(tMax, tFar);
        }

        if (tMax < tMin || tMax < 0.) {
            return Double.NaN;
        }

        return tMax;
    }
}

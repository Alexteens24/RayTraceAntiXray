package com.vanillage.raytraceantixray.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

import org.bukkit.World;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

class VectorialLocationTest {

    @Test
    void comparesWorldReferentsPositionAndDirectionByValue() {
        World world = mock(World.class);
        World otherWorld = mock(World.class);
        VectorialLocation location = new VectorialLocation(world, new Vector(1.0, 2.0, 3.0), new Vector(0.0, 1.0, 0.0));

        assertEquals(location, new VectorialLocation(world, new Vector(1.0, 2.0, 3.0), new Vector(0.0, 1.0, 0.0)));
        assertEquals(location.hashCode(), new VectorialLocation(world, new Vector(1.0, 2.0, 3.0), new Vector(0.0, 1.0, 0.0)).hashCode());
        assertNotEquals(location, new VectorialLocation(world, new Vector(1.1, 2.0, 3.0), new Vector(0.0, 1.0, 0.0)));
        assertNotEquals(location, new VectorialLocation(world, new Vector(1.0, 2.0, 3.0), new Vector(1.0, 0.0, 0.0)));
        assertNotEquals(location, new VectorialLocation(otherWorld, new Vector(1.0, 2.0, 3.0), new Vector(0.0, 1.0, 0.0)));
    }
}

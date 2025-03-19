package com.graphhopper.gtfs;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class EdgeVerticesTest {

    @Test
    public void testEqual() {
        EdgeVertices a = new EdgeVertices(1, 2);
        EdgeVertices b = new EdgeVertices(1, 2);
        assertEquals(a, b);
    }

    @Test
    public void testHash() {
        EdgeVertices a = new EdgeVertices(1, 2);
        int value = 3;
        HashMap<EdgeVertices, Integer> map = new HashMap<>();
        map.put(a, value);

        assertEquals(value, map.get(new EdgeVertices(1, 2)));
    }
}

package bfst22.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import bfst22.vector.model.OSMNode;
import bfst22.vector.model.WayType;
import bfst22.vector.model.ShortesPath.Edge;
import bfst22.vector.model.ShortesPath.TravelType;

public class EdgeTest {
    @Test
    void testEdgeTime() {
        Edge edge = new Edge(new ArrayList<OSMNode>(), WayType.RESIDENTIALROAD, 000, 000, 50000, 50, null, false);
        assertEquals(3600, edge.getTimetoTraverse(TravelType.CAR));
    }
}

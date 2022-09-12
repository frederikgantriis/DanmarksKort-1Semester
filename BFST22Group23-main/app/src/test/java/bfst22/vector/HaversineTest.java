package bfst22.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import bfst22.vector.model.Model;
import bfst22.vector.model.OSMNode;

import org.junit.jupiter.api.Test;

public class HaversineTest {
    @Test
    void testHaversine1() {
        Model model;
        double distance = -2;
        try{
        model = new Model("data/Baagoe.osm");
        distance = Model.haversine(new OSMNode(1, (float)(Math.cos(Math.toRadians(model.getMinlat())) * 10.569416), (float)(55.840061)), new OSMNode(2, (float)(Math.cos(Math.toRadians(model.getMinlat())) * 10.573386), (float)(55.839296)));
        }catch(Exception e){}

        assertEquals(262, (int)distance);
    }
}

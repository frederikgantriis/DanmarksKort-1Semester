package bfst22.vector;

import static org.junit.jupiter.api.Assertions.assertTrue;

import bfst22.vector.model.Model;

import org.junit.jupiter.api.Test;

public class AngelBetweenPointsTest {
    @Test
    void testLeftTurn() {
        Model model;
        double angel = -2;
        try{
        model = new Model("data/Baagoe.osm");
        angel = model.angelBetweenPoints(12.618956* Math.cos(Math.toRadians(model.getMinlat())), -55.635337, 12.619628* Math.cos(Math.toRadians(model.getMinlat())), -55.635416, 12.619467* Math.cos(Math.toRadians(model.getMinlat())), -55.635773);
        }catch(Exception e){}
        assertTrue(angel < 72.5 && angel > 72.4);
    }
    @Test
    void testRightTurn() {
        Model model;
        double angel = -2;
        try{
        model = new Model("data/Baagoe.osm");
        angel = model.angelBetweenPoints((float)(9.807867* Math.cos(Math.toRadians(model.getMinlat()))), (float)-55.314251, (float)(9.807478* Math.cos(Math.toRadians(model.getMinlat()))), (float)-55.314265, (float)(9.807478* Math.cos(Math.toRadians(model.getMinlat()))), (float)-55.314447);
        }catch(Exception e){}
        assertTrue(angel < 92.3 && angel > 92.2);
    }
    @Test
    void testLeftTurn2() {
        Model model;
        double angel = -2;
        try{
        model = new Model("data/Baagoe.osm");
        angel = model.angelBetweenPoints((float)(9.807867* Math.cos(Math.toRadians(model.getMinlat()))), (float)-55.314251, (float)(9.807478* Math.cos(Math.toRadians(model.getMinlat()))), (float)-55.314265, (float)(9.807462* Math.cos(Math.toRadians(model.getMinlat()))), (float)-55.314095);
        }catch(Exception e){}
        assertTrue(angel < 93.1 && angel > 93);
    }
    @Test
    void testBearing(){
        double angel = -2;
        try {
            Model model = new Model("data/Baagoe.osm");
            angel = model.compasBearing((float)(-94.581213* Math.cos(Math.toRadians(model.getMinlat()))), (float)39.099912, (float)(-90.200203* Math.cos(Math.toRadians(model.getMinlat()))), (float)38.627089);
            angel = -angel;
        } catch (Exception e) {}
        assertTrue(83.4 < angel && angel < 83.5);
    } 
    @Test
    void testLeftTurnWithBearing() {
        Model model;
        double angel = 0;
        try{
        model = new Model("data/Baagoe.osm");
        angel = model.turnAngel((float)(9.807867* Math.cos(Math.toRadians(model.getMinlat()))), (float)-55.314251, (float)(9.807478* Math.cos(Math.toRadians(model.getMinlat()))), (float)-55.314265, (float)(9.807462* Math.cos(Math.toRadians(model.getMinlat()))), (float)-55.314095);
        }catch(Exception e){}
        assertTrue(260< angel && angel < 280);
    }
    @Test
    void testRightTurnWithBearing() {
        Model model;
        double angel = 0;
        try{
        model = new Model("data/Baagoe.osm");
        angel = model.turnAngel((float)(9.807867* Math.cos(Math.toRadians(model.getMinlat()))), (float)-55.314251, (float)(9.807478* Math.cos(Math.toRadians(model.getMinlat()))), (float)-55.314265, (float)(9.807478* Math.cos(Math.toRadians(model.getMinlat()))), (float)-55.314447);
        }catch(Exception e){}
        assertTrue(80 < angel && angel < 100);
    }
}

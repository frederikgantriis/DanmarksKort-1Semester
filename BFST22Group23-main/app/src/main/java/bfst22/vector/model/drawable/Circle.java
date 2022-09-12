package bfst22.vector.model.drawable;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Circle implements Drawable {
    Color color;
    public double lon;
    public double lat;
    int size;

    public Circle(Color color, double lon, double lat, int size){
        this.color = color;
        this.lon = lon;
        this.lat = lat;
        this.size = size;
    }

    @Override
    public void trace(GraphicsContext gc) {        
    }
    public Color getColor(){
        return color;
    }
    public double getLon(){return lon;}
    public double getLat(){return lat;}

    public int getSize() {
        return size;
    }

    @Override
    public void fillSimplified(GraphicsContext gc, Point2D leftBottom, Point2D rightTop, double zoom) {
    }
}

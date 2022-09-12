package bfst22.vector.model;

import java.io.Serializable;

import javafx.geometry.Point2D;

public class OSMNode implements Serializable {
    public static final long serialVersionUID = 9082413;
    private long id;
    private float lon, lat;

    public OSMNode(long id, float lon, float lat) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
    }

    public float getLatitude() {
        return lat;
    }

    public float getLongitude() {
        return lon;
    }

    public long getId(){
        return id;
    }

    public Point2D getPoint2D() {
        return new Point2D(lon, lat);
    }
}
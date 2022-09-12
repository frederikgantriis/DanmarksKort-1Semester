package bfst22.vector.model.ShortesPath;

import java.util.ArrayList;

public class Route {
    private double distance;
    private int timeToTraverse;
    private ArrayList<Edge> edges;

    public Route(double distance, int timeToTraverse) {
        this.distance = distance;
        this.timeToTraverse = timeToTraverse;
        edges = new ArrayList<>();
    }

    public void add(Edge item) {
        edges.add(item);
    }
    
    public int size() {
       return edges.size();
    }

    public ArrayList<Edge> getEdges(){
        return edges;
    }

    public double getDistance() {
        return distance;
    }

    public int getTimeToTraverse() {
        return timeToTraverse;
    }
}

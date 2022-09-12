package bfst22.vector.model.ShortesPath;
import java.util.Collection;

import bfst22.vector.model.Model;
import bfst22.vector.model.OSMNode;
import bfst22.vector.model.WayType;
import bfst22.vector.model.drawable.PolyLine;


public class Edge extends PolyLine{
    public static final long serialVersionUID = 863758;
    private double distance;
    private long fromID;
    private long toID;
    private WayType type;
    private int speedlimit;
    private String roadName;
    private boolean isOneway;
  
    public Edge(Collection<OSMNode> nodes, WayType type, long fromID, long toID, double distance, int speedlimit, String roadName, boolean isOneway){
        super(nodes);
        this.distance = distance;
        this.fromID = fromID;
        this.toID = toID;
        this.type = type;
        this.speedlimit = speedlimit;
        this.roadName = roadName;
        this.isOneway = isOneway;
    }
  
    public Edge(float[] coords, WayType type, long fromID, long toID, double distance, int speedlimit, String roadName, boolean isOneway){
        super(coords);
        this.distance = distance;
        this.fromID = fromID;
        this.toID = toID;
        this.type = type;
        this.speedlimit = speedlimit;
        this.roadName = roadName;
        this.isOneway = isOneway;
    }

    public final RoadNode getDestination(long id){
        return id == toID ? Model.getRoadNode(fromID) : Model.getRoadNode(toID);
    }
    /**
     * Method for getting the distance of an edge.
     * @return Returns the length of this edge in meters.
     */
    public final double getDistance(){
        return distance;
    }
    public final WayType getType(){
        return type;
    }
    public int getSpeedlimit(){
        return speedlimit;
    }
    public boolean isFrom(long id){
        return id == fromID;
    }
    public boolean isTo(long id){
        return id == toID;
    }
    public int findCommonNode(Edge edge){
        long fromID = edge.getFrom().getId();
        long toID = edge.getTo().getId();
        if (isFrom(fromID)) return 1;
        if (isTo(fromID)) return 2;
        if (isFrom(toID)) return 3;
        if (isTo(toID)) return 4; 
        return 0;
    }

    /**
     * Method for getting the time it takes to traverse this edge by a given form of transport.
     * @return Returns the time it takes in seconds as an int to traverse the edge by a given type of transport (TravelType).
     */
    public final int getTimetoTraverse(TravelType travelType){
        switch (travelType) {
            case CAR:
                return (int)(distance/(double)(speedlimit/3.6));
            case BICYCLE:
                return (int)(distance/(double)(travelType.getMaxSpeed()*0.6/3.6));
            case FOOT:
                return (int)(distance/(double)(travelType.getMaxSpeed()*0.6/3.6));
        }
        return -1;
    }

    public final boolean supportsTransportBy(TravelType travelType) {
        return type.supportsTransportBy(travelType);
    }

    public final RoadNode getFrom() {
        return Model.getRoadNode(fromID);
    }

    public final RoadNode getTo() {
        return Model.getRoadNode(toID);
    }
    //returns the coords of from and to Roadnode
    public float[] getNodeCoords(long nodeid){
        float[] nodecoords = new float[2];
        if (nodeid == fromID) {
            nodecoords[0] = super.coords[0];
            nodecoords[1] = super.coords[1];
        }else{
            nodecoords[0] = super.coords[coords.length-2];
            nodecoords[1] = super.coords[coords.length-1];
        }
        return nodecoords;
    }

    public String getRoadName() {
        return roadName;
    }

    public boolean isOneway(){
        return isOneway;
    }
}

package bfst22.vector.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import bfst22.vector.App;
import bfst22.vector.Main;
import bfst22.vector.model.KdTree.KDTree;
import bfst22.vector.model.KdTree.Point;
import bfst22.vector.model.ShortesPath.Edge;
import bfst22.vector.model.ShortesPath.IndexMinPQ;
import bfst22.vector.model.ShortesPath.RoadNode;
import bfst22.vector.model.ShortesPath.Route;
import bfst22.vector.model.ShortesPath.TravelType;
import bfst22.vector.model.drawable.Drawable;
import bfst22.vector.model.drawable.KDTreeSubdivide;
import bfst22.vector.model.drawable.MultiPolygon;
import bfst22.vector.model.drawable.PolyLine;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javafx.geometry.Point2D;
import javafx.scene.paint.CycleMethod;

public class Model {
    private static float minlat;
    private float minlon;
    private float maxlat;
    private float maxlon;
    private Map<WayType,List<Drawable>> lines = new EnumMap<>(WayType.class); {
        for (var type : WayType.values()) lines.put(type, new ArrayList<>());
    }
    private List<Runnable> observers = new ArrayList<>();
    private KDTree kdTree;
    private TST tst;
    private ArrayList<Edge> dijkstraEdges = new ArrayList<>();
    private static HashMap<Long, RoadNode> roadMap = new HashMap<>();

    public ArrayList<Edge> getDijkstrasEdges(){
        return dijkstraEdges;
    }

    public float getMaxlat() {
        return maxlat;
    }

    public float getMaxlon() {
        return maxlon;
    }

    public float getMinlat() {
        return minlat;
    }

    public float getMinlon() {
        return minlon;
    }

    @SuppressWarnings("unchecked")
    public Model(String filename) throws IOException, XMLStreamException, FactoryConfigurationError, ClassNotFoundException {
        var time = -System.nanoTime();
        InputStream fileInputStream;
        try {
            fileInputStream = App.class.getResource(filename).openStream();
        }
        catch (NullPointerException e) {
            fileInputStream = new FileInputStream(filename);
        }
        //var file = resource.getFile();
        if (filename.endsWith(".zip")) {
            var zip = new ZipInputStream(fileInputStream);
            zip.getNextEntry();
            loadOSM(zip);
        } else if (filename.endsWith(".osm")) {
            loadOSM(fileInputStream);
        } else if (filename.endsWith(".obj")) {
            try (var input = new ObjectInputStream(new BufferedInputStream(fileInputStream))) {
                minlat = input.readFloat();
                minlon = input.readFloat();
                maxlat = input.readFloat();
                maxlon = input.readFloat();
                lines = (Map<WayType,List<Drawable>>) input.readObject();
                kdTree = new KDTree((Point) input.readObject());
                tst = new TST((TSTNode)input.readObject());
                roadMap = (HashMap<Long, RoadNode>) input.readObject();
            }
        }
        time += System.nanoTime();
        System.out.println("Load time: " + (long)(time / 1e6) + " ms");
        if(!filename.endsWith(".obj")) save(filename);
    }

    public void save(String basename) throws FileNotFoundException, IOException {
        long timestamp = -System.nanoTime();
        try (var out = new ObjectOutputStream(new FileOutputStream(basename + ".obj"))) {
            out.writeFloat(minlat);
            out.writeFloat(minlon);
            out.writeFloat(maxlat);
            out.writeFloat(maxlon);
            out.writeObject(lines);
            out.writeObject(kdTree.getRoot());
            out.writeObject(tst.getRoot());
            out.writeObject(roadMap);
            
            System.out.println("Time to Save: " +(long)((timestamp + System.nanoTime()) / 1e6) + " ms");
            out.close();
        }
    }

    public static RoadNode getRoadNode(long key) {
        return roadMap.get(key);
    }

    private void loadOSM(InputStream input) throws XMLStreamException, FactoryConfigurationError {
        var reader = XMLInputFactory.newInstance().createXMLStreamReader(new BufferedInputStream(input));
        tst = new TST();
        var id2node = new NodeMap();
        var id2way = new HashMap<Long, PolyLine>();
        var nodes = new ArrayList<OSMNode>();
        var outerRel = new ArrayList<PolyLine>();
        var innerRel = new ArrayList<PolyLine>();
        var memberIDs = new ArrayList<Long>();
        HashMap<WayType, HashMap<Long, PolyLine>> wayreferences = new HashMap<WayType, HashMap<Long, PolyLine>>(4);
                wayreferences.put(WayType.COASTLINE, new HashMap<Long, PolyLine>());
                wayreferences.put(WayType.LAKE, new HashMap<Long, PolyLine>());
                wayreferences.put(WayType.FOREST, new HashMap<Long, PolyLine>());
                wayreferences.put(WayType.UNKNOWN, new HashMap<Long, PolyLine>());
                wayreferences.put(WayType.GRASS, new HashMap<Long, PolyLine>());
                wayreferences.put(WayType.BUILDING, new HashMap<Long, PolyLine>());
        var address = new AddressBuilder();
        long relID = 0;
        var type = WayType.UNKNOWN;
        boolean isFirstway = true;
        boolean jylland = false;
        boolean road = false;
        long timestamp = -System.nanoTime();
        int speedlimit= 10;
        boolean isOneway = false;
        String roadName = "Unknown roadName";
        roadMap = new HashMap<Long, RoadNode>();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    var name = reader.getLocalName();
                    switch (name) {
                        case "bounds":
                            maxlat = -Float.parseFloat(reader.getAttributeValue(null, "minlat"));
                            minlat = -Float.parseFloat(reader.getAttributeValue(null, "maxlat"));
                            minlon = (float)Math.cos(Math.toRadians(minlat)) * Float.parseFloat(reader.getAttributeValue(null, "minlon"));
                            maxlon = (float)Math.cos(Math.toRadians(minlat)) * Float.parseFloat(reader.getAttributeValue(null, "maxlon"));
                            break;
                        case "node":
                            var id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            var lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                            var lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                            id2node.add(new OSMNode(id, (float)(Math.cos(Math.toRadians(minlat)) * lon), -lat));
                            break;
                        case "nd":
                            var ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            nodes.add(id2node.get(ref));
                            break;
                        case "way":
                            if(isFirstway){
                                System.out.println("Time to load Nodes: " +(long)((timestamp + System.nanoTime()) / 1e6) + " ms");
                                timestamp = -System.nanoTime();
                                kdTree = new KDTree(id2node);
                                System.out.println("Time to load KdTree: " +(long)((timestamp + System.nanoTime()) / 1e6) + " ms");
                                timestamp = -System.nanoTime();
                                isFirstway = false;
                            }
                            relID = Long.parseLong(reader.getAttributeValue(null, "id"));
                            type = WayType.UNKNOWN;
                            isOneway = false;
                            break;
                        case "tag":
                            var k = reader.getAttributeValue(null, "k");
                            var v = reader.getAttributeValue(null, "v");
                            if (!isFirstway) {
                                switch (k) {
                                    case "name":
                                        if (road) {
                                            roadName = v;
                                        }
                                        break;
                                    case "building":
                                        type = WayType.BUILDING;
                                        break;
                                    case "area":
                                        type = WayType.AREA;
                                        break;
                                    case "tourism":
                                        switch (v) {
                                            case "camp_site":
                                                type = WayType.CAMPSITE;
                                                break;
                                        }
                                        break;
                                    case "natural":
                                        switch (v) {
                                            case "water":
                                                type = WayType.LAKE;
                                                break;
                                            case "coastline":
                                                type = WayType.COASTLINE;
                                                break;
                                            case "wetland":
                                                type = WayType.WETLAND;
                                                break;
                                            case "grassland":
                                                type = WayType.GRASSLAND;
                                                break;
                                            case "scrub":
                                                type = WayType.SCRUB;
                                                break;
                                        }
                                        break;
                                    case "landuse":
                                        switch (v) {
                                            case "forest":
                                                type = WayType.FOREST;
                                                break;
                                            case "farmland":
                                                type = WayType.FARMLAND;
                                                break;
                                            case "residential":
                                                type = WayType.RESIDENTIAL;
                                                break;
                                            case "meadow":
                                                type = WayType.MEADOW;
                                                break;
                                            case "grass":
                                                type = WayType.GRASS;
                                                break;
                                            case "recreation_ground":
                                                type = WayType.GRASS;
                                                break;
                                            case "industrial":
                                                type = WayType.RESIDENTIAL;
                                                break;
                                        }
                                        break;
                                    case "highway":
                                        road = true;
                                        speedlimit = 10;
                                        switch (v) {
                                            case "path":
                                                type = WayType.PATH;
                                                break;
                                            case "unclassified":
                                                type = WayType.UNCLASSIFIEDHIGHWAY;
                                                break;
                                            case "track":
                                                type = WayType.TRACK;
                                                break;
                                            case "motorway":
                                                type = WayType.MOTORWAY;
                                                break;
                                            case "trunk":
                                                type = WayType.TRUNK;
                                                break;
                                            case "primary":
                                                type = WayType.PRIMARY;
                                                break;
                                            case "secondary":
                                                type = WayType.SECONDARY;
                                                break;
                                            case "tertiary":
                                                type = WayType.TERTIARY;
                                                break;
                                            case "residential":
                                                type = WayType.RESIDENTIALROAD;
                                                break;
                                            case "service":
                                                type = WayType.SERVICEROAD;
                                                break;
                                            case "cycleway":
                                                type = WayType.CYCLEWAY;
                                                break;
                                            default:
                                                type = WayType.UNCLASSIFIEDHIGHWAY;
                                                break;
                                        }
                                        break;
                                    case "route":
                                        switch (v) {
                                            case "ferry":
                                                type = WayType.FERRY;
                                                break;
                                        }
                                        break;
                                    case "place":
                                        switch(v){
                                            case "island":
                                                type = WayType.COASTLINE;
                                                break;
                                            case "peninsula":
                                                type = WayType.COASTLINE;
                                                break;
                                            case "islet":
                                                type = WayType.COASTLINE;
                                                break;
                                        }
                                        break;
                                    case "name:it":
                                        if (v.equals("Jutland")) {
                                            jylland = true;
                                        }
                                        break;
                                        case "maxspeed":
                                        try{
                                        speedlimit = Integer.parseInt(reader.getAttributeValue(null, "v"));
                                        }catch (Exception e) {
                                            //dosen't handle "defualt", "signals" and "implicit" speedlimits
                                        }
                                        break;
                                    case "oneway":
                                        if (v.equals("yes")) {
                                            isOneway = true;
                                        }
                                        break;
                                    case "junction":
                                        if (v.equals("roundabout")) {
                                            isOneway = true;
                                        }
                                    case "leisure":
                                        switch (v) {
                                            case "park":
                                                type = WayType.GRASS;
                                                break;
                                            case "garden":
                                                type = WayType.GRASS;
                                                break;
                                            case "pitch":
                                                type = WayType.GRASS;
                                                break;
                                            case "track":
                                                type = WayType.GRASS;
                                                break;
                                            case "golf_course":
                                                type = WayType.GRASS;
                                                break;
                                            case "stadium":
                                                type = WayType.GRASS;
                                                break;
                                            case "sports_centre":
                                                type = WayType.GRASS;
                                                break;
                                            case "dog_park":
                                                type = WayType.GRASS;
                                                break;
                                        }
                                        break;
                                }
                            }else{
                                switch (k) {
                                    case "addr:city":
                                        address.city(v);
                                        break;
                                    case "addr:housenumber":
                                        address.house(v);
                                        break;
                                    case "addr:floor":
                                        address.floor(v);
                                        break;
                                    case "addr:side":
                                        address.side(v);
                                        break;
                                    case "addr:postcode":
                                        address.postcode(v);
                                        break;
                                    case "addr:street":
                                        address.street(v);
                                        tst.put(address.build().toString().toLowerCase(), id2node.get(id2node.size() - 1));
                                        break;
                                    }
                            }
                            break;

                        case "member":
                            ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            var elm = id2way.get(ref);
                            if (elm != null){
                                memberIDs.add(ref);
                                String role = reader.getAttributeValue(null, "role");
                                if (role.equals("inner")) {
                                    innerRel.add(elm);
                                }else{
                                    outerRel.add(elm);
                                }
                            }
                            break;
                        case "relation":
                            id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            type = WayType.UNKNOWN;
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "way":
                            float avgLatitude = 0;
                            float avgLongtitude = 0;
                            if (wayreferences.keySet().contains(type)) {
                                PolyLine way = new PolyLine(nodes);
                                wayreferences.get(type).put(relID, way);
                                id2way.put(relID, way);

                            }else if (road) {
                                RoadNode previousRoadNode;
                                long nodeid = nodes.get(0).getId();
                                if (roadMap.containsKey(nodeid)) {
                                    previousRoadNode = roadMap.get(nodeid);
                                }else{
                                    previousRoadNode = new RoadNode(nodeid);
                                    roadMap.put(nodeid, previousRoadNode);
                                }
                                double distance = 0;
                                List<OSMNode> newEdgeNodes;
                                for (int i = 1; i < nodes.size(); i++) {
                                    nodeid = nodes.get(i).getId();
                                    newEdgeNodes = nodes.subList(i-1, i+1);
                                    distance = haversine(newEdgeNodes.get(0), newEdgeNodes.get(1));
                                    RoadNode newNode;
                                    if (roadMap.containsKey(nodeid)) {
                                        newNode = roadMap.get(nodeid);
                                    }else{
                                        newNode = new RoadNode(nodeid);
                                        roadMap.put(nodeid, newNode);
                                    }
                                    
                                    Edge edge = new Edge(newEdgeNodes, type, previousRoadNode.getId(), newNode.getId(), distance, speedlimit, roadName, isOneway);
                                    previousRoadNode.addEdge(edge);
                                    newNode.addEdge(edge);
                                    previousRoadNode = newNode;
                                }
                                road = false;
                                roadName = "unnamed road";
                            }else{
                                PolyLine way = new PolyLine(nodes);
                                for (OSMNode node : nodes) {
                                    avgLatitude += node.getLatitude();
                                    avgLongtitude += node.getLongitude();
                                }
                                avgLatitude = avgLatitude/nodes.size();
                                avgLongtitude = avgLongtitude/nodes.size();
                                EnumMap<WayType, List<Drawable>> enumMap = kdTree.FindDrawableList(avgLatitude, avgLongtitude);
                                enumMap.get(type).add(way);  
                            }
                            nodes.clear();
                            break;
                        case "relation":
                            if (type != WayType.UNKNOWN && type != WayType.COASTLINE && wayreferences.keySet().contains(type) && !outerRel.isEmpty()) {
                                avgLatitude = 0;
                                avgLongtitude = 0; 
                                int relsSize = 0;
                                for (PolyLine polyline : outerRel) {
                                    float[] coords = polyline.getCoords();
                                    for (int i = 0; i < coords.length; i += 2) {
                                        avgLongtitude += coords[i];
                                        avgLatitude += coords[i+1];
                                        relsSize++;
                                    }
                                }
                                avgLatitude = avgLatitude/relsSize;
                                avgLongtitude = avgLongtitude/relsSize;
                                EnumMap<WayType, List<Drawable>> enumMap = kdTree.FindDrawableList(avgLatitude, avgLongtitude, true, kdTree.getRoot());
                                for (long id : memberIDs) {
                                    if (wayreferences.get(WayType.UNKNOWN).remove(id) == null){
                                        wayreferences.get(type).remove(id);
                                    }
                                }
                                memberIDs.clear();
                                MultiPolygon multiPolygon;
                                if (!innerRel.isEmpty()) {
                                    multiPolygon =  new MultiPolygon(outerRel, new MultiPolygon(innerRel)); 
                                }else{
                                    multiPolygon = new MultiPolygon(outerRel);
                                }
                                enumMap.get(type).add(multiPolygon);

                            }else if (type == WayType.COASTLINE && !outerRel.isEmpty()) {
                                if (jylland) {
                                    System.out.println("fuck jylland");
                                    jylland = false;
                                }else{
                                    for (long id : memberIDs) {
                                        if (wayreferences.get(type).remove(id) == null){
                                            wayreferences.get(WayType.UNKNOWN).remove(id);
                                        }
                                    }
                                    memberIDs.clear();
                                    lines.get(type).add(new MultiPolygon(outerRel));
                                }
                            }
                            innerRel.clear();
                            outerRel.clear();
                            break;
                    }
                    break;
            }
        }
        for (WayType wayType : wayreferences.keySet()) {
            for (PolyLine polyLine : wayreferences.get(wayType).values()) {
                float[] coords = polyLine.getCoords();
                float avgLatitude = 0;
                float avgLongtitude = 0;
                for (int i = 0; i < coords.length; i += 2) {
                    avgLongtitude += coords[i];
                    avgLatitude += coords[i+1];
                }
                avgLongtitude = avgLongtitude/(coords.length/2);
                avgLatitude = avgLatitude/(coords.length/2);
                EnumMap<WayType, List<Drawable>> enumMap = kdTree.FindDrawableList(avgLatitude, avgLongtitude);
                enumMap.get(wayType).add(polyLine);
            }
        }
        cleanupRoadMap(roadMap);
        
        for (long id : roadMap.keySet()) {
            for (Edge edge : roadMap.get(id).getEdges()) {
                float[] coords = edge.getCoords();
                float avgLatitude = 0;
                float avgLongtitude = 0;
                for (int i = 0; i < coords.length; i += 2) {
                    avgLongtitude += coords[i];
                    avgLatitude += coords[i+1];
                }
                avgLongtitude = avgLongtitude/(coords.length/2);
                avgLatitude = avgLatitude/(coords.length/2);
                EnumMap<WayType, List<Drawable>> enumMap = kdTree.FindDrawableList(avgLatitude, avgLongtitude);
                enumMap.get(edge.getType()).add(edge);
            }
        } 
        System.out.println("Time to load Ways & Relations: " +(long)((timestamp + System.nanoTime()) / 1e6) + " ms");
    }

    /**
     * Calculates the distance between two given nodes.
     * @param node1 First node of the calculation of type OSMNode.
     * @param node2 Second node of the calculation of type OSMNode.
     * @return Returns distance between node1 and node2 in meters.
     */
    public static double haversine(OSMNode node1, OSMNode node2)
    {
        return haversine(node1.getPoint2D(), node2.getPoint2D());
    }

    public static double haversine(Point2D point1, Point2D point2) {
        return haversine(point1.getY(), point1.getX(), point2.getY(), point2.getX());
    }


    public static double haversine(float point1lon, float point1lat, Point2D point2) {
        return haversine(point1lat, point1lon, point2.getY(), point2.getX());
    }

    public static double haversine(double lat1, double longtitude1, double lat2, double longtitude2) {
        double lon1 = longtitude1 / Math.cos(Math.toRadians(minlat));
        double lon2 = longtitude2 / Math.cos(Math.toRadians(minlat));
        
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
 
        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.pow(Math.sin(dLon / 2), 2) *
                   Math.cos(lat1) *
                   Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return (float)(rad * c)*1000;
    }


    public void addObserver(Runnable observer) {
        observers.add(observer);
    }

    public void notifyObservers() {
        for (var observer : observers) {
            observer.run();
        }
    }

    public Iterable<Drawable> iterable(WayType type) {
        return lines.get(type);
    }
    public Map<WayType, List<Drawable>> getLines(){
        return lines;
    }

    public KDTree getKDTree(){
        return kdTree;
    }

    public TST getTST(){
        return tst;
    }

    
    public EnumMap<WayType, List<Drawable>> kdTreeIterable(Point2D leftBottom, Point2D rightTop, double zoomLevel){
        if (zoomLevel < 0.5) {
            double zoomFactor = (0.5-zoomLevel)*2;
            double zoomMultiplier = 0.2+zoomFactor;
            double windowWidth = rightTop.getX() - leftBottom.getX();
            double windowHeight = leftBottom.getY() - rightTop.getY();
            //System.out.println(zoomMultiplier);
            Point2D newLB = new Point2D(leftBottom.getX() - windowWidth*zoomMultiplier, leftBottom.getY() + windowHeight*zoomMultiplier);
            Point2D newRT = new Point2D(rightTop.getX() + windowWidth*zoomMultiplier, rightTop.getY() - windowHeight*zoomMultiplier);
            return kdTree.rangeSearch(newLB, newRT, maxlon, minlon, maxlat, minlat, zoomLevel);
        }
        else {
            return kdTree.rangeSearch(leftBottom, rightTop, maxlon, minlon, maxlat, minlat, zoomLevel);
        }
    }

    public ArrayDeque<KDTreeSubdivide> getKDTreeSplits() {
        return kdTree.getDebugRangeQueue();
    }

    public Edge getNearestEdge(double lon, double lat) {
        return kdTree.getNearestEdge(lon, lat);
    }

    public Route dijkstra(RoadNode startNode, RoadNode endNode, TravelType travelType) {
        dijkstraEdges = new ArrayList<>();
        
        float[] endNodecoords = endNode.getEdges().get(0).getCoords();
        HashMap<Long, Edge> edgeTo = new HashMap<>();
        HashMap<Long, Double> distanceTo = new HashMap<>();
        HashMap<Long, Integer> timeTo = new HashMap<>();
        IndexMinPQ queue = new IndexMinPQ();
        distanceTo.put(startNode.getId(), 0.0);
        timeTo.put(startNode.getId(), 0);
        queue.insert(startNode, 0);
        boolean found = false;

        while(!queue.isEmpty() && !found){
            RoadNode node = queue.deleteMinimum();
            long nodeID = node.getId();
            for (Edge edge : node.getEdges()) {
                if (!edge.supportsTransportBy(travelType)) {
                    continue;
                }
                if (edge.isOneway()) {
                    if (!(edge.getFrom().getId() == nodeID)) {
                        continue;
                    }
                }
                
                RoadNode toNode = edge.getDestination(nodeID);
                
                long toNodeID = toNode.getId();
                int newTime = timeTo.get(nodeID) + edge.getTimetoTraverse(travelType);
                if (timeTo.get(toNodeID) == null || newTime < timeTo.get(toNodeID)) {
                    dijkstraEdges.add(edge);
                    timeTo.put(toNodeID, newTime);
                    distanceTo.put(toNodeID, distanceTo.get(nodeID) + edge.getDistance());
                    edgeTo.put(toNodeID, edge);

                    float[] tonodecoords = edge.getNodeCoords(toNodeID);
                    double distanceToEndNode = haversine(tonodecoords[0], tonodecoords[1], endNodecoords[0], endNodecoords[1]);
                    double timeToEndNode = distanceToEndNode/((travelType.getMaxSpeed())/3.6);
                    double weight = timeToEndNode + timeTo.get(toNodeID);
                    //boolean motorway = WayType.MOTORWAY == edge.getType();
                    
                    if (queue.contains(edge.getDestination(nodeID))) {
                        queue.change(toNode, weight);
                    }
                    else {
                        queue.insert(toNode, weight);
                    }
                    
                }
                if (endNode.getId() == toNodeID){ 
                    found = true;
                    break;
                }
            }
        }

        if (distanceTo.get(endNode.getId()) == null) {
            System.out.println("Can't find route between start node and end node!");
            return null;
        }

        Route route = new Route((double)distanceTo.get(endNode.getId()), timeTo.get(endNode.getId()));
        RoadNode current = endNode;
        while (current != startNode) {
            Edge routeEdge = edgeTo.get(current.getId());
            route.add(routeEdge);
            current = routeEdge.getDestination(current.getId());
        }
        return route;
    }
    private void cleanupRoadMap(HashMap<Long, RoadNode> roadMap){
        ArrayList<Long> removedNodes = new ArrayList<>();
        for (RoadNode roadNode : roadMap.values()) {
            ArrayList<Edge> edges = roadNode.getEdges();
            if (edges.size() == 2) {
                Edge edge1 = edges.get(0);
                Edge edge2 = edges.get(1);
                if (edge1.getType() == edge2.getType() && edge1.getSpeedlimit() == edge2.getSpeedlimit() && !edge1.isOneway() && !edge2.isOneway()) {                
                    RoadNode fromNode = edge1.getDestination(roadNode.getId());
                    fromNode.getEdges().remove(edge1);
                    RoadNode toNode = edge2.getDestination(roadNode.getId());
                    toNode.getEdges().remove(edge2);

                    if (edge1.getFirstX() == edge2.getFirstX() && edge1.getFirstY() == edge2.getFirstY() || edge1.getFirstX() == edge2.getLastX() && edge1.getFirstY() == edge2.getLastY())
                        edge1.reverse();
                    if (edge1.getLastX() == edge2.getLastX() && edge1.getLastY() == edge2.getLastY()) {
                        edge2.reverse();
                    } 
                    
                    float[] coords = new float[edge1.getCoords().length + edge2.getCoords().length - 2];
                    
                    for (int i = 0; i < edge1.getCoords().length; i++) {
                        coords[i] = edge1.getCoords()[i];
                    }
                    for (int i = 2; i < edge2.getCoords().length; i++) {
                        coords[i+edge1.getCoords().length-2] = edge2.getCoords()[i];
                    }

                    Edge edge = new Edge(coords, edge1.getType(), fromNode.getId(), toNode.getId(), edge1.getDistance()+edge2.getDistance(), edge1.getSpeedlimit(), edge1.getRoadName(), false);
                    fromNode.addEdge(edge);
                    toNode.addEdge(edge);

                    removedNodes.add(roadNode.getId());
                }
            }
        }
        for (Long id : removedNodes) {
            roadMap.remove(id);
        }
    }

    public ArrayList<String> getRouteDirections(Route route){
        ArrayList<String> directions = new ArrayList<>();
        ArrayList<Edge> edges = route.getEdges();
        if (edges.size() < 2) {
            return null;
        }
        Edge prevoisEdge = edges.get(edges.size()-1);
        Edge currentEdge = edges.get(edges.size()-2);
        boolean isPreviousFrom = false, isCurrentFrom = false;
        switch (currentEdge.findCommonNode(prevoisEdge)){
            case 1:
                isPreviousFrom = true;
                isCurrentFrom = true;
                break;
            case 2:
                isPreviousFrom = true;
                isCurrentFrom = false;
                break;
            case 3:
                isPreviousFrom = false;
                isCurrentFrom = true;
                break;
            case 4:
                isPreviousFrom = false;
                isCurrentFrom = false;
                break;
        }
        int distance = addDirection(directions, prevoisEdge, currentEdge, isPreviousFrom, isCurrentFrom, 0);

        for (int i = edges.size()-3; i >= 0; i--) {
            prevoisEdge = currentEdge;
            isPreviousFrom =!isCurrentFrom;
            currentEdge = edges.get(i);
            if (isPreviousFrom) {
                isCurrentFrom = currentEdge.isFrom(prevoisEdge.getFrom().getId());
            }else{
                isCurrentFrom = currentEdge.isFrom(prevoisEdge.getTo().getId());
            }
            distance = addDirection(directions, prevoisEdge, currentEdge, isPreviousFrom, isCurrentFrom, distance);
        }
        directions.add("Arrive after: " + (int)Math.ceil(distance + edges.get(0).getDistance()) + " meters.");
        
        return directions;
    }

    public int addDirection(ArrayList<String> directions, Edge prevoisEdge, Edge currentEdge, boolean isPreviousFrom, boolean isCurrentFrom, int distance){
        float from_longtitude, from_latitude, intersection_longtitude, intersection_latitude, to_longtitude, to_latitude;
        float[] prevoiscoords = prevoisEdge.getCoords();
        float[] currentcoords = currentEdge.getCoords();
        if (isPreviousFrom) {
            from_longtitude = prevoiscoords[2];
            from_latitude = prevoiscoords[3];
            intersection_longtitude = prevoiscoords[0];
            intersection_latitude = prevoiscoords[1];
        }else{
            int lengt = prevoiscoords.length;
            from_longtitude = prevoiscoords[lengt-4];
            from_latitude = prevoiscoords[lengt-3];
            intersection_longtitude = prevoiscoords[lengt-2];
            intersection_latitude = prevoiscoords[lengt-1];
        }
        if (isCurrentFrom) {
            to_longtitude = currentcoords[2];
            to_latitude = currentcoords[3];
        }else{
            to_longtitude = currentcoords[currentcoords.length-4];
            to_latitude = currentcoords[currentcoords.length-3];
        }

        distance += prevoisEdge.getDistance();
        distance = (int)Math.ceil(distance);
        double angel = angelBetweenPoints(from_longtitude, from_latitude, intersection_longtitude, intersection_latitude, to_longtitude, to_latitude);
        if (160 < angel && angel <= 180) {  
            //directions.add("go straight");
            distance += currentEdge.getDistance();
        }else if (0 < angel && angel < 20) {
            directions.add("Turn around after: " + distance + " meters.");
            distance = 0;
        }else{
            angel = turnAngel(from_longtitude, from_latitude, intersection_longtitude, intersection_latitude, to_longtitude, to_latitude);
            if(180 < angel && angel < 360) {
                directions.add("Turn left after: " + distance + " meters.");
                distance = 0;
            }else if(0 < angel && angel < 180){
                directions.add("Turn right after: " + distance + " meters.");
                distance = 0;
            }
        }
        return distance;
    }
    
    public double angelBetweenPoints(double from_longtitude, double from_latitude, double intersection_longtitude, double intersection_latitude, double to_longtitude, double to_latitude){
        from_longtitude = from_longtitude / Math.cos(Math.toRadians(getMinlat()));
        intersection_longtitude = intersection_longtitude / Math.cos(Math.toRadians(getMinlat()));
        to_longtitude = to_longtitude / Math.cos(Math.toRadians(getMinlat()));

        double a = 100000*Math.sqrt(Math.pow(intersection_longtitude-from_longtitude, 2)+ Math.pow(intersection_latitude-from_latitude, 2));
        double b = 100000*Math.sqrt(Math.pow(to_longtitude-intersection_longtitude, 2)+ Math.pow(to_latitude-intersection_latitude, 2));
        double c = 100000*Math.sqrt(Math.pow(to_longtitude-from_longtitude, 2)+ Math.pow(to_latitude-from_latitude, 2));
        double roadAngel = (a*a + b*b - c*c)/(2*a*b);
        roadAngel = Math.toDegrees(Math.acos(roadAngel));
        return roadAngel;
    }

    public double turnAngel(double from_longtitude, double from_latitude, double intersection_longtitude, double intersection_latitude, double to_longtitude, double to_latitude){
        double fromBearing = compasBearing(from_longtitude, from_latitude, intersection_longtitude, intersection_latitude);
        double toBearing = compasBearing(intersection_longtitude, intersection_latitude, to_longtitude, to_latitude);
        double angel = fromBearing-toBearing;
        /* left 270 & -90 degrees
           rigth -270 & 90 degrees*/
        return angel > 0 ? angel : (Math.abs(angel)+180) % 360;
    }

    public double compasBearing(double from_longtitude, double from_latitude, double to_longtitude, double to_latitude){
        from_longtitude = from_longtitude / Math.cos(Math.toRadians(getMinlat()));
        to_longtitude = to_longtitude / Math.cos(Math.toRadians(getMinlat()));
        from_latitude = -from_latitude;
        to_latitude = -to_latitude;

        /* Formula to find bearing
        X = cos θb * sin ∆L
        Y = cos θa * sin θb – sin θa * cos θb * cos ∆L 
        β = atan2(X,Y),*/
        double x = (cos(to_latitude)) * (sin(to_longtitude-from_longtitude));
        double y = cos(from_latitude)*sin(to_latitude) - sin(from_latitude) * cos(to_latitude) * cos(to_longtitude-from_longtitude);
        double bearing = Math.toDegrees(Math.atan2(x, y));

        return -bearing;
    }
    private double cos(double x){
        return Math.cos(Math.toRadians(x));
    }
    private double sin(double x){
        return Math.sin(Math.toRadians(x));
    }
}

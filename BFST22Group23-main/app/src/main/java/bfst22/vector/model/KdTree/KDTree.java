package bfst22.vector.model.KdTree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

import bfst22.vector.model.Model;
import bfst22.vector.model.NodeMap;
import bfst22.vector.model.OSMNode;
import bfst22.vector.model.WayType;
import bfst22.vector.model.ShortesPath.Edge;
import bfst22.vector.model.drawable.Drawable;
import bfst22.vector.model.drawable.KDTreeSubdivide;
import javafx.geometry.Point2D;

public class KDTree{
    private Point root;
    private ArrayDeque<KDTreeSubdivide> debugRangeQueue;

    public KDTree(NodeMap nodemap){
        createTree(nodemap);
    }
    public KDTree(Point root){
        this.root = root;
    }
    //They compare X = longtitude and Y = latitude
    private final Comparator<OSMNode> cmpX = new Comparator<OSMNode>() {
        @Override
        public int compare(OSMNode p1, OSMNode p2) {
            return Float.compare(p1.getLongitude(), p2.getLongitude());
        }
    };
    private final Comparator<OSMNode> cmpY = new Comparator<OSMNode>() {
        @Override
        public int compare(OSMNode p1, OSMNode p2) {
            return Float.compare(-p1.getLatitude(), -p2.getLatitude());
        }
    };

    //Makes the root of the tree
    private void createTree(List<OSMNode> nodeList) {
        MedianSort.medianSort(nodeList, cmpX);
        root = new Point(nodeList.get(nodeList.size()/2).getLongitude());
        root.setLeftChild(recursivCall(nodeList.subList(0, nodeList.size()/2), false));    
        root.setRightChild(recursivCall(nodeList.subList(nodeList.size()/2, nodeList.size()), false));
    }

    //Makes all points after the root
    private Point recursivCall(List<OSMNode> nodelist, boolean CompareX){ 
        MedianSort.medianSort(nodelist, CompareX ? cmpX : cmpY);
        float value = CompareX ? nodelist.get(nodelist.size()/2).getLongitude() : nodelist.get(nodelist.size()/2).getLatitude();
        Point point = new Point(value);

        int endAmount = 2000;
        if (nodelist.size() > endAmount) {
            point.setLeftChild(recursivCall(nodelist.subList(0, nodelist.size()/2), !CompareX));    
            point.setRightChild(recursivCall(nodelist.subList(nodelist.size()/2, nodelist.size()), !CompareX)); 
        }
        else {
            MedianSort.medianSort(nodelist, !CompareX ? cmpX : cmpY);
            point.setLeftChild(new FinalPoint(nodelist.subList(0, nodelist.size()/2), !CompareX));
            point.setRightChild(new FinalPoint(nodelist.subList(nodelist.size()/2, nodelist.size()), !CompareX));
        }
        return point;
    }
    
    public Point getRoot(){
        return root;
    }

    // Drawing tree in debug mode
    public KDTreeQueue getKDTreeQueue() {
        KDTreeQueue queue = new KDTreeQueue();
        buildKDTreeQueue(root, queue, true);
        return queue;
    }

    private void buildKDTreeQueue(Point node, KDTreeQueue queue, boolean isX) {
        queue.enqueue(node.getValue(), isX);

        if (node instanceof FinalPoint) return;

        buildKDTreeQueue(node.getLeftChild(), queue, !isX);
        buildKDTreeQueue(node.getRightChild(), queue, !isX);
    }

     //Range search
     public EnumMap<WayType, List<Drawable>> rangeSearch(Point2D leftBottom, Point2D rightTop, double maxLon, double minlon, double maxLat, double minLat, double zoomLevel) {
        EnumMap<WayType, List<Drawable>> map = new EnumMap<>(WayType.class);
        float minY = (float) minLat;
        float maxY = (float) maxLat;
        float maxX = (float) maxLon;
        float minX = (float) minlon;
        debugRangeQueue = new ArrayDeque<>();
        rangeSearch(root, map, leftBottom, rightTop, true, minY, maxY, minX, maxX, zoomLevel);
        return map;
    }
    
    private void rangeSearch(Point point, EnumMap<WayType, List<Drawable>> map, Point2D leftBottom, Point2D rightTop, boolean compareX, Float minY, Float maxY, Float minX, Float maxX, double zoomLevel) {
        int cmplo;
        int cmphi;
        KDTreeSubdivide split;

        if (compareX) {
            cmplo = Float.compare((float)leftBottom.getX(), point.getValue());
            cmphi = Float.compare((float)rightTop.getX(), point.getValue());

            split = new KDTreeSubdivide(point.getValue(), minY, point.getValue(), maxY);
        } else {
            cmplo = -Float.compare((float)leftBottom.getY(), point.getValue());
            cmphi = -Float.compare((float)rightTop.getY(), point.getValue());

            split = new KDTreeSubdivide(minX, point.getValue(), maxX, point.getValue());
        }
        debugRangeQueue.add(split);
        
        if (point instanceof FinalPoint) {
            ArrayList<EnumMap<WayType, List<Drawable>>> children = new ArrayList<>();
            FinalPoint x = (FinalPoint)point;
            if (cmplo < 0) {
                children.add(x.getDrawablesLeftChild());
                }
            if (cmphi > 0) {
                children.add(x.getDrawablesRightChild());
            }
            for (var enumMap : children) {
                for (WayType type : enumMap.keySet()) {
                    if (!type.visibleByZoomLevel(zoomLevel))
                        continue;
                    if (map.get(type) == null)
                        map.put(type, new ArrayList<Drawable>());
                    map.get(type).addAll(enumMap.get(type));
                }
            }
            return;
        } 
        
        if (cmplo < 0) {
            if (compareX) {
                rangeSearch(point.getLeftChild(), map, leftBottom, rightTop, !compareX, minY, maxY, minX, point.getValue(), zoomLevel);
            } else {
                rangeSearch(point.getLeftChild(), map, leftBottom, rightTop, !compareX, point.getValue(), maxY, minX, maxX, zoomLevel);
            }
              
        } 
        if (cmphi > 0){
            if (compareX) {
                rangeSearch(point.getRightChild(), map, leftBottom, rightTop, !compareX, minY, maxY, point.getValue(), maxX, zoomLevel);
            } else {
                rangeSearch(point.getRightChild(), map, leftBottom, rightTop, !compareX, minY, point.getValue(), minX, maxX, zoomLevel);
            }
        };
    }

    public ArrayDeque<KDTreeSubdivide> getDebugRangeQueue() {
        return debugRangeQueue;
    }

    public Edge getNearestEdge(double lon,double lat){
        Point2D mainPoint = new Point2D(lon, lat);
        return getNearestEdge(mainPoint, this.root, true, null);
    }
    private Edge getNearestEdge(Point2D mainPoint, Point root, boolean compareX, Edge curClosestEdge){
        int comparator;

        if (compareX) {
            comparator = Double.compare(mainPoint.getX(), root.getValue());
        } else{
            comparator = -Double.compare(mainPoint.getY(), root.getValue());
        }

        //If the split is a FinalPoint, then we have find the closest node
        if (root instanceof FinalPoint) {
            FinalPoint curFinalPoint = (FinalPoint) root;
            EnumMap<WayType, List<Drawable>> enumMap;

            //Decides which side of the split we are on
            if (comparator <= 0) {
                enumMap = curFinalPoint.getDrawablesLeftChild();
            } else {
                enumMap = curFinalPoint.getDrawablesRightChild();
            }

            //Gets the closest node in the current FinalPoint
            curClosestEdge = getClosestNodeInFinalPoint(enumMap, mainPoint, compareX, curClosestEdge);

            //Check if the split is closer than the current closest node
            if (curClosestEdge == null || isSplitCloser(curClosestEdge, root, mainPoint, compareX)) {

                //Takes the opposite side of the split
                if (comparator > 0) {
                    enumMap = curFinalPoint.getDrawablesLeftChild();
                } else {
                    enumMap = curFinalPoint.getDrawablesRightChild();
                }

                //Gets the closest node in the opposite side of the split
                Edge tempEdge = getClosestNodeInFinalPoint(enumMap, mainPoint, compareX, curClosestEdge);
        
                //If the opposite side of the split is closer than the current closest node, then we take that one
                if (curClosestEdge == null || getDistToNode(tempEdge, mainPoint) < getDistToNode(curClosestEdge, mainPoint)) {
                    curClosestEdge = tempEdge;
                }

            }
            return curClosestEdge;
        }

        //Check which side of the split the point is on
        if (comparator <= 0) {
            curClosestEdge = getNearestEdge(mainPoint, root.getLeftChild(), !compareX, curClosestEdge);
        } else {
            curClosestEdge = getNearestEdge(mainPoint, root.getRightChild(), !compareX, curClosestEdge);
        }

        // If the split is closer than the current closest node, then we need to check the other side of the split.
        if (curClosestEdge == null || isSplitCloser(curClosestEdge, root, mainPoint, compareX)) {
            Edge tempEdge;
            if (comparator > 0) {
                tempEdge = getNearestEdge(mainPoint, root.getLeftChild(), !compareX, curClosestEdge);
            } else {
                tempEdge = getNearestEdge(mainPoint, root.getRightChild(), !compareX, curClosestEdge);
            }

            if (curClosestEdge == null || getDistToNode(tempEdge, mainPoint) < getDistToNode(curClosestEdge, mainPoint)) {
                curClosestEdge = tempEdge;
            }
        }

        return curClosestEdge;
    }

    private boolean isSplitCloser(Edge curClosestEdge, Point split, Point2D mainPoint, boolean compareX){
        double distToSplit;
        if (compareX) {
            distToSplit = Model.haversine(mainPoint.getY(), mainPoint.getX(), mainPoint.getY(), split.getValue());
        } else{
            distToSplit = Model.haversine(mainPoint.getY(), mainPoint.getX(), split.getValue(), mainPoint.getX());
        }
        return distToSplit < getDistToNode(curClosestEdge, mainPoint);
    }

    private Edge getClosestNodeInFinalPoint(EnumMap<WayType, List<Drawable>> enumMap, Point2D mainPoint, boolean compareX, Edge curClosestEdge) throws NullPointerException {

        for (WayType wayType : WayType.roadKeySet()) {
            List<Drawable> drawables = enumMap.get(wayType);
            
            for (Drawable drawable : drawables) {
                if (drawable instanceof Edge) {
                    Edge tempEdge = (Edge) drawable;
                    if (curClosestEdge == null || getDistToNode(tempEdge, mainPoint) < getDistToNode(curClosestEdge, mainPoint) ) {
                    curClosestEdge = tempEdge;
                    }    
                    
                }
            }
        }
        return curClosestEdge;
    }

    private float getDistToNode(Edge edge, Point2D mainPoint){
        return (float) Math.min(Model.haversine(edge.getFirstX(), edge.getFirstY(), mainPoint), Model.haversine(edge.getLastX(), edge.getLastY(), mainPoint));
    }

    public EnumMap<WayType, List<Drawable>> FindDrawableList(float lat, float lon){
        return FindDrawableList(lat, lon, true, root);
    }

    //reangeSearch but finds only one kdtree leaf
    public EnumMap<WayType, List<Drawable>> FindDrawableList(float lat, float lon, boolean CompareX, Point root){
        if (root instanceof FinalPoint)  {
            FinalPoint point = (FinalPoint) root;
            if (CompareX) {
                if (root.getValue() > lon) {
                    return point.getDrawablesLeftChild();
                } else{
                    return point.getDrawablesRightChild();
                }
            }else{
                if (root.getValue() < lat) {
                    return point.getDrawablesLeftChild();
                } else{
                    return point.getDrawablesRightChild();
                }
            }
        }else if (CompareX) {
            if (root.getValue() > lon) {
                return FindDrawableList(lat, lon, !CompareX, root.getLeftChild());
            } else{
                return FindDrawableList(lat, lon, !CompareX, root.getRightChild());
            }
        }else{
            if (root.getValue() < lat) {
                return FindDrawableList(lat, lon, !CompareX, root.getLeftChild());
            } else{
                return FindDrawableList(lat, lon, !CompareX, root.getRightChild());
            }
        }
    }

    
    
    
    
    
}

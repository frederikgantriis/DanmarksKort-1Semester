package bfst22.vector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.EnumMap;
import java.util.List;
import java.util.Queue;

import bfst22.vector.model.Model;
import bfst22.vector.model.OSMNode;
import bfst22.vector.model.WayType;
import bfst22.vector.model.ShortesPath.Edge;
import bfst22.vector.model.ShortesPath.Route;
import bfst22.vector.model.ShortesPath.TravelType;
import bfst22.vector.model.Theme;
import bfst22.vector.model.drawable.Circle;
import bfst22.vector.model.drawable.Drawable;
import bfst22.vector.model.drawable.KDTreeSubdivide;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.FillRule;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

public class MapCanvas extends Canvas {
    Model model;
    Affine trans = new Affine();
    GraphicsContext gc = getGraphicsContext2D();
    boolean first = true;
    private boolean debuggingEnabled;
    private boolean kdTreeEnabled;
    private boolean debuggingDrawOnlyDijkstra;
    private Point2D leftBottom;
    private Point2D rightTop;
    private double debugWindowRelation = 0.3;
    private EnumMap<WayType, List<Drawable>> drawables;
    private Theme theme;
    private Route route;
    private Edge routeStartEdge, routeEndEdge;
    private TravelType type = TravelType.CAR;

    public void init(Model model) {
        setDebugMode(false);
        theme = Theme.LIGHT;
        this.model = model;
        pan(-model.getMinlon(), -model.getMinlat());
        zoom(640 / (model.getMaxlon() - model.getMinlon()), 0, 0);
        model.addObserver(this::repaint);
        gc.setFillRule(FillRule.EVEN_ODD);
    }

    public Route getRoute(){
        return route;
    }

    public void drawMap(){
        gc.beginPath();
        gc.setTransform(new Affine());
        switch (theme) {
            case DARK:
                gc.setFill(Color.valueOf("#1B262C"));
                break;
            default:
                gc.setFill(Color.valueOf("#B8FFF9"));
                break;
        }
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setTransform(trans);

        drawPolygon(WayType.COASTLINE);
        
        gc.setLineWidth(1/Math.sqrt(trans.determinant()));
        for (WayType type : drawables.keySet()) {
            gc.setStroke(type.getEdgePaint(theme));
            if (type.shouldFill()) {
                gc.setFill(type.getFillPaint(theme));
                for (Drawable line : drawables.get(type)) {
                    line.fill(gc);
                }
            }
            else {
                for (Drawable line :drawables.get(type)) {
                    line.draw(gc);
                }
            }
        } 
        drawPoint(WayType.MOUSEMARK);
    }

    public void drawdijkstraMap(){
        gc.beginPath();
        gc.setTransform(new Affine());
        switch (theme) {
            case LIGHT:
                gc.setFill(Color.valueOf("#B8FFF9"));
                break;
            case DARK:
            gc.setFill(Color.valueOf("#1B262C"));
            break;
            default:
            gc.setFill(Color.valueOf("#B8FFF9"));
            break;
        }
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setTransform(trans);

        drawPolygon(WayType.COASTLINE);

        if(!model.getDijkstrasEdges().isEmpty()){
            for (Edge edge : model.getDijkstrasEdges()) {
                edge.trace(gc);
            }
            gc.stroke();
        }  
    }
    
    public double getZoom() {
        return pixelXtoLon(1000) - pixelXtoLon(0);
    }
    
    public void repaint() {
        updateDrawingRectangle();
        drawables = model.kdTreeIterable(leftBottom, rightTop, getZoom());
        if (isDebuggingDrawOnlyDijkstra()) {
            drawdijkstraMap();;
        } else {
            drawMap();
        }
        if (isDebugging()) {
            // Draw debug rectangle (proportional to screen size)
            gc.beginPath();
            var linewidth = gc.getLineWidth();
            gc.setLineWidth(linewidth*5);
            gc.setStroke(Color.DARKBLUE);
            gc.moveTo(leftBottom.getX(), leftBottom.getY());
            gc.lineTo(rightTop.getX(), leftBottom.getY());
            gc.lineTo(rightTop.getX(), rightTop.getY());
            gc.lineTo(leftBottom.getX(), rightTop.getY());
            gc.lineTo(leftBottom.getX(), leftBottom.getY());
            gc.stroke();
            gc.setLineWidth(linewidth);
            gc.setStroke(Color.BLACK);
        }
        if (isKdTreeEnabled()) {
            drawKdTree();
        }

        if (route != null && route.size() > 0)
            drawRoute(route);
    }


    public boolean isKdTreeEnabled() {
        return kdTreeEnabled;
    }
    public void drawRoute(Route route) {
        gc.beginPath();
        gc.setStroke(Color.RED);
        var lineWidth = gc.getLineWidth();
        gc.setLineWidth(lineWidth*5);
        for (Edge edge : route.getEdges()) {
            edge.trace(gc);
        }
        gc.stroke();
        gc.setLineWidth(lineWidth);
    }

    public void setRouteStart(Edge startEdge) {
        routeStartEdge = startEdge;
        updateRoute();
    }
    public void setRouteEnd(Edge endEdge) {
        routeEndEdge = endEdge;
        updateRoute();
    }
    public void setRoute(Route newRoute) {
        route = newRoute;
    }
    public boolean isStartRoute() {
        return routeStartEdge != null;
    }
    public boolean isEndRoute() {
        return routeEndEdge != null;
    }
    public boolean isRoute(){
        return route != null;
    }

    private void updateRoute() {
        if (routeStartEdge != null && routeEndEdge != null) {
            route = model.dijkstra(routeStartEdge.getFrom(), routeEndEdge.getFrom(), getTravelType());
            if (route != null)
                drawRoute(route);
        }
        else {
            route = null;
        }
    }

    public TravelType getTravelType(){
        return type;
    }

    public void changeTravelType(TravelType type){
        this.type = type;
        updateRoute();
    }
    
    public void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
    }

    public void zoom(double factor, double x, double y) {
        trans.prependTranslation(-x, -y);
        trans.prependScale(factor, factor);
        if (!(getZoom() > 0.002 && getZoom() < 6) && !first) {
            trans.prependScale(1/factor, 1/factor);
        }
        trans.prependTranslation(x, y);
        if (first) {
            first = false;
        }
    }

    public Point2D mouseToModel(Point2D point) {
        try {
            return trans.inverseTransform(point);
        } catch (NonInvertibleTransformException e) {
            throw new RuntimeException(e);
        }
    }
    public Point2D modelToScreen(Point2D point) {
        return trans.transform(point);
    }

    public void drawPolygon(WayType wayType){
        gc.setLineWidth(1/Math.sqrt(trans.determinant()));
        String filename = "textures\\\\" + wayType.toString() + "Texture.jpg";
        try {
            gc.setFill(new ImagePattern(new Image(new FileInputStream(filename)), 0, 0, 1, 1, true));
        } catch (FileNotFoundException exception) {
            gc.setFill(wayType.getFillPaint(theme));
        }
        gc.setStroke(wayType.getEdgePaint(theme));

        double factor = 0.3;
        Point2D lb = new Point2D(leftBottom.getX()-(rightTop.getX()-leftBottom.getX())*factor, leftBottom.getY()+(leftBottom.getY()-rightTop.getY())*factor);
        Point2D rt = new Point2D(rightTop.getX()+(rightTop.getX()-leftBottom.getX())*factor, rightTop.getY()-(leftBottom.getY()-rightTop.getY())*factor);
        for (var line : model.iterable(wayType)) {
            line.fillSimplified(gc, lb, rt, getZoom());
        }
    }

    public void drawLine(Color color, WayType wayType){
        gc.setLineWidth(1/Math.sqrt(trans.determinant()));
        gc.setStroke(color);
        for (var line : model.iterable(wayType)) {
            line.draw(gc);
        }
        gc.setStroke(Color.BLACK);
    }

    public void drawPoint(WayType wayType){
        drawLine(Color.RED, wayType);
        for (var circle1 : model.iterable(wayType)) {
            Circle circle = (Circle) circle1;
            Color color = circle.getColor();
            double xcord = circle.getLon();
            double ycord = circle.getLat();
            gc.setFill(color);
            gc.fillOval((xcord-(circle.getSize()/trans.getMxx()) / 2), (ycord-(circle.getSize()/trans.getMxx()) / 2), circle.getSize()/trans.getMxx(), circle.getSize()/trans.getMxx());
        }
    }

    public void addPoint(Circle circle, WayType wayType) {
        circle.lon = pixelXtoLon(circle.lon);
        circle.lat = pixelYtoLat(circle.lat);
        model.getLines().get(wayType).add(circle);
    }

    public void addAddressPoint(Circle circle, WayType wayType){
        model.getLines().get(wayType).add(circle);
    }

    public double pixelXtoLon(double pixelX){
       return 1/trans.getMxx()*pixelX-trans.getTx()/trans.getMxx();
    }
    public double pixelYtoLat(double pixelY){
        return 1/trans.getMxx()*pixelY-trans.getTy()/trans.getMxx();
    }
    public boolean isDebuggingDrawOnlyDijkstra() {
        return debuggingDrawOnlyDijkstra;
    }
    public void toggleDebugDrawOnlyDijkstraMode() {
        debuggingDrawOnlyDijkstra = !isDebuggingDrawOnlyDijkstra();
        updateDrawingRectangle();
    }
    public void toggleDebugMode() {
        debuggingEnabled = !isDebugging();
        updateDrawingRectangle();
    }
    public void setDebugMode(boolean enabled) {
        debuggingEnabled = enabled;
        updateDrawingRectangle();
    }
    public boolean isDebugging() {
        return debuggingEnabled;
    }

    public void updateDrawingRectangle() {
        double width = getWidth();
        double height = getHeight();
        double debugWidth = width*debugWindowRelation;
        double debugHeight = height*debugWindowRelation;
        if (isDebugging()) {
            leftBottom = mouseToModel(new Point2D((width-debugWidth)/2, height-(height-debugHeight)/2));
            rightTop = mouseToModel(new Point2D(width-(width-debugWidth)/2, (height-debugHeight)/2));
        }
        else {
            leftBottom = mouseToModel(new Point2D(0, height));
            rightTop = mouseToModel(new Point2D(width, 0));
        }
    }

    // Getter methods for debug window positions
    public Point2D getLeftBottom() {
        return leftBottom;
    }
    public Point2D getRightTop() {
        return rightTop;
    }

    public void drawKdTree(){
        Queue<KDTreeSubdivide> queue = model.getKDTreeSplits();
        gc.beginPath();
        gc.setStroke(Color.RED);
        drawKDTreeSplits(queue);
        gc.setStroke(Color.BLACK);
    }

    private void drawKDTreeSplits(Queue<KDTreeSubdivide> queue) {
        while (!queue.isEmpty()) {
            KDTreeSubdivide split = queue.poll();
            gc.moveTo(split.getStartPointX(), split.getStartPointY());
            gc.lineTo(split.getEndPointX(), split.getEndPointY());
            gc.stroke();
        }
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public void setDebugWindowRelation(double d) {
        debugWindowRelation = d;
        updateDrawingRectangle();
    }

    public void setKdTreeEnabled(boolean kdTreeEnabled) {
        this.kdTreeEnabled = kdTreeEnabled;
    }

    public Edge getNearestEdge(double x, double y) {
        Edge nearestNode = model.getNearestEdge(pixelXtoLon(x), pixelYtoLat(y));
        return nearestNode;
    }
    public Edge getNearestAdress(double x, double y) {
        Edge nearestNode = model.getNearestEdge(x, y);
        return nearestNode;
    }

    public void drawRedNode(OSMNode node) {
        double circleRadius = 0.0003;
        gc.setLineWidth(circleRadius/10);
        gc.moveTo(node.getLongitude(), node.getLatitude());
        gc.setFill(Color.RED);
        gc.fillOval(node.getLongitude()-(circleRadius/5), node.getLatitude()-(circleRadius/5), circleRadius, circleRadius);
    }

    public int getZoomInMeters() {
        return (int) Math.ceil(Model.haversine(model.getMinlat(), pixelXtoLon(100) , model.getMinlat(), pixelXtoLon(0)));
    }
}
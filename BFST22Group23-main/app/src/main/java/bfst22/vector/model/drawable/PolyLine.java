package bfst22.vector.model.drawable;

import java.io.Serializable;
import java.util.Collection;

import bfst22.vector.model.OSMNode;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

public class PolyLine implements Drawable, Serializable {
    public static final long serialVersionUID = 134123;
    protected float[] coords;
    private boolean shouldJump = true;

    public PolyLine(Collection<OSMNode> nodes) {
        coords = new float[nodes.size() * 2];
        int i = 0;
        for (var node : nodes) {
            coords[i++] = node.getLongitude();
            coords[i++] = node.getLatitude();
        }
    }
    
    public PolyLine(float[] coords){
        this.coords = coords;
    }

    @Override
    public void trace(GraphicsContext gc) {
        int i = 0;
        if (shouldJump) {
            gc.moveTo(coords[0], coords[1]);
            i = 2;
        }
        for (; i < coords.length; i += 2) {
            gc.lineTo(coords[i], coords[i+1]);
        }
    }

    public void fillSimplified(GraphicsContext gc, Point2D leftBottom, Point2D rightTop, double zoom) {
        gc.beginPath();
        traceSimplified(gc, leftBottom, rightTop, zoom);
        gc.fill();
    }

    public void traceSimplified(GraphicsContext gc, Point2D leftBottom, Point2D rightTop, double zoom) {
        int i = 0;
        if (shouldJump) {
            gc.moveTo(coords[0], coords[1]);
            i = 2;
        }
        for (; i < coords.length; i += 2) {
            int timeout = 0;
            int zoomTimeout = 0;
            if (i >= 2 && !inScreen(coords[i-2], coords[i-1], leftBottom, rightTop)) {
                timeout = 100;
            }

            // skip nodes in coastlines
            if (zoom > 1) {
                zoomTimeout = 8;
            }

            while (i < coords.length-2 && timeout > 0 && !inScreen(coords[i], coords[i+1], leftBottom, rightTop)) {
                i += 2;
                timeout -= 2;
            }
            while (zoomTimeout > 0) {
                i += 2;
                zoomTimeout -= 2;
            }
            if (i < coords.length)
                gc.lineTo(coords[i], coords[i+1]);
        }
    }
    private boolean inScreen(float x, float y, Point2D leftBottom, Point2D rightTop) {
        return x > leftBottom.getX() && x < rightTop.getX() && y < leftBottom.getY() && y > rightTop.getY();
    }

    public void setJump(boolean jump) {
        shouldJump = jump;
    }

    public float[] getCoords() {
        return coords;
    }

    public void reverse() {
        for (int i = 0; i < coords.length/2; i += 2) {
            exchange(i, coords.length-2-i);
            exchange(i+1, coords.length-1-i);
        }
    }

    private void exchange(int a, int b) {
        float aux = coords[a];
        coords[a] = coords[b];
        coords[b] = aux;
    }
    
    public float getFirstX() {
        return coords[0];
    }
    public float getLastX() {
        return coords[coords.length-2];
    }
    public float getFirstY() {
        return coords[1];
    }
    public float getLastY() {
        return coords[coords.length-1];
    }
}

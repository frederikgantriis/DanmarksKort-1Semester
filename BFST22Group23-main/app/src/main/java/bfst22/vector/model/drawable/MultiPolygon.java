package bfst22.vector.model.drawable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

public class MultiPolygon implements Drawable, Serializable {
    public static final long serialVersionUID = 1325234;
    private List<PolyLine> parts;
    private MultiPolygon innerMultiPolygon;
    
    public MultiPolygon(ArrayList<PolyLine> rel) {
        parts = new ArrayList<>();
        parts.add(rel.remove(0));
        
        if (!rel.isEmpty()) {
            PolyLine first = parts.get(0);
            PolyLine second = rel.get(0);

            if (first.getFirstX() == second.getFirstX() && first.getFirstY() == second.getFirstY() || first.getFirstX() == second.getLastX() && first.getFirstY() == second.getLastY())
                first.reverse();
        }
        
        while(!rel.isEmpty()) {
            PolyLine last = parts.get(parts.size()-1);

            PolyLine foundPart = null;
            for (PolyLine part : rel) {
                if (part.getFirstX() == last.getLastX() && part.getFirstY() == last.getLastY()) {
                    foundPart = part;
                    break;
                }
                else if (part.getLastX() == last.getLastX() && part.getLastY() == last.getLastY()) {
                    part.reverse();
                    foundPart = part;
                    break;
                }
            }
            if (foundPart == null) {
                parts.add(rel.remove(0));
            }
            else {
                foundPart.setJump(false);
                parts.add(foundPart);
                rel.remove(foundPart);
            }
        }
    }

    public MultiPolygon(ArrayList<PolyLine> rel, MultiPolygon innerMultiPolygon){
        this(rel);
        this.innerMultiPolygon = innerMultiPolygon;
    }
    
    public void trace(GraphicsContext gc) {
        if (innerMultiPolygon != null) {        
            innerMultiPolygon.trace(gc);
        }
         for (int i = 0; i < parts.size(); i++) {
            parts.get(i).trace(gc);
        }   
    }
    
    public void fillSimplified(GraphicsContext gc, Point2D leftBottom, Point2D rightTop, double zoom) {
        gc.beginPath();
        for (var part : parts) {
            part.traceSimplified(gc, leftBottom, rightTop, zoom);
        }
        gc.fill();
    }
}

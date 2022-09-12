package bfst22.vector.model.drawable;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

public interface Drawable {
    default void draw(GraphicsContext gc) {
        gc.beginPath();
        trace(gc);
        gc.stroke();
    }
    default void fill(GraphicsContext gc) {
        gc.beginPath();
        trace(gc);
        gc.fill();
    }
    public void trace(GraphicsContext gc);
    public void fillSimplified(GraphicsContext gc, Point2D leftBottom, Point2D rightTop, double zoom);
}

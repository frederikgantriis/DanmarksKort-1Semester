package bfst22.vector;

import javafx.animation.AnimationTimer;

public class RepaintAnimationTimer extends AnimationTimer{

    MapCanvas canvas;
    boolean isPanning = false;
    boolean isZooming = false;
    double middleX;
    double middleY;
    double xPan;
    double yPan;

    public RepaintAnimationTimer(MapCanvas canvas) {
        super();
        this.canvas = canvas;
    }

    @Override
    public void handle(long now) {
        canvas.repaint();
        if (isPanning) {
            autoPan();
        }
        if (isZooming) {
            autoZoom();
        }
    }

    public void startAutoPan(double xPan, double yPan){
        this.xPan = xPan;
        this.yPan = yPan;
        isPanning = true;
    }
    public void startAutoZoom(double middleX, double middleY){
        this.middleX = middleX;
        this.middleY = middleY;
        isZooming = true;
    }
    int x = 0, y = 0, panF = 100;
    public void autoPan(){
        if (x < xPan) {
            if (xPan < 0) {
                canvas.pan(-panF, 0);
            } else {
                canvas.pan(panF, 0);
            }
            x += panF;
        }
        if (y < yPan) {
            if (yPan < 0) {
                canvas.pan(-panF, 0);
            } else {
                canvas.pan(panF, 0);
            }
            y += panF;
        }
        if (x >= xPan && y >= yPan) {
            isPanning = false;
            x = 0;
            y = 0;
        }
    }
    
    public void autoZoom(){
        if (canvas.getZoom() > 0.005) {
            canvas.zoom(1.25, middleX, middleY);
        } else {
            isZooming = false;
        }
    }
}

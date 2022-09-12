package bfst22.vector.model.drawable;

public class KDTreeSubdivide {
    //{startpoint x, startpoint y, endpoint x, endpoint y}
    private float[] points;


    public KDTreeSubdivide(float startpointX, float startpointY, float endpointX, float endpointY) {
        points = new float[]{startpointX, startpointY, endpointX, endpointY};
    }

    public float getStartPointX() {
        return points[0];
    }

    public float getStartPointY() {
        return points[1];
    }

    public float getEndPointX() {
        return points[2];
    }

    public float getEndPointY() {
        return points[3];
    }

    public boolean isX() {
        return getStartPointX() == getEndPointX();
    }

        
}


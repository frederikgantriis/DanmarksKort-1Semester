package bfst22.vector.model.ShortesPath;

public enum TravelType {
    //this is maxspeed for A* the actual speed of foot is 5 and bicycle is 20 as seen in Edge.getTimetoTraverse()
    CAR(130), FOOT(8), BICYCLE(30);
    private int maxspeed;
    TravelType(int maxspeed){
        this.maxspeed = maxspeed;
    }
    public int getMaxSpeed(){
        return maxspeed;
    }
}


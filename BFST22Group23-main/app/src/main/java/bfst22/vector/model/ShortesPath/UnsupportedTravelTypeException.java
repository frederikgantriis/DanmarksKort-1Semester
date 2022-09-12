package bfst22.vector.model.ShortesPath;

public class UnsupportedTravelTypeException extends RuntimeException {
    public UnsupportedTravelTypeException(TravelType travelType) {
        super(travelType.toString());
    }
}

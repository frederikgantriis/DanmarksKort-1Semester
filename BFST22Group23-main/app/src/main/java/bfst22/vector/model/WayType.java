package bfst22.vector.model;

import java.util.HashSet;
import java.util.Set;

import bfst22.vector.ZoomLevels;
import bfst22.vector.model.ShortesPath.TravelType;
import bfst22.vector.model.ShortesPath.UnsupportedTravelTypeException;
import javafx.scene.paint.*;

public enum WayType {
    COASTLINE(ZoomLevels.zoomLevel0, new Color[]{Color.BLACK, null, null}, new Color[]{Color.valueOf("#F2EFE9"), Color.valueOf("#303841"), null}),
    FOREST(ZoomLevels.zoomLevel2, new Color[3], new Color[]{Color.valueOf("#ADD19E"), Color.valueOf("#02383C"), null}){
        @Override
        public String toString(){
            return "Forest";
        }
    },

    AREA(ZoomLevels.zoomLevel10, new Color[3], new Color[]{Color.RED, null, null}),
    FARMLAND(ZoomLevels.zoomLevel3, new Color[3], new Color[]{Color.valueOf("#EEF0D5"), Color.valueOf("#423F3E"), null}),
    GRASS(ZoomLevels.zoomLevel3, new Color[3], new Color[]{Color.valueOf("#DDFFBC"), Color.valueOf("#191A19"), null}),
    GRASSLAND(ZoomLevels.zoomLevel3, new Color[3], new Color[]{Color.valueOf("#CDEBB0"), Color.valueOf("#062925"), null}),
    MEADOW(ZoomLevels.zoomLevel4, new Color[3], new Color[]{Color.valueOf("#DDFFBC"), Color.valueOf("#191A19"), null}),
    WETLAND(ZoomLevels.zoomLevel4, new Color[3], new Color[]{Color.valueOf("#A1CAE2"), null, null}),
    RESIDENTIAL(ZoomLevels.zoomLevel4, new Color[3], new Color[]{Color.valueOf("#E0DFDF"), Color.valueOf("#282D4F"), null}),
    CAMPSITE(ZoomLevels.zoomLevel4,new Color[3], new Color[]{Color.valueOf("#DEF6C0"), Color.valueOf("#62374E"), null}),
    SCRUB(ZoomLevels.zoomLevel4, new Color[3], new Color[]{Color.LIGHTSTEELBLUE, Color.valueOf("#191A19"), null}),
    MOUSEMARK(ZoomLevels.zoomLevel10, new Color[]{Color.RED, null, null}), 
    LAKE(ZoomLevels.zoomLevel1, new Color[3], new Color[]{Color.valueOf("#B8FFF9"), Color.valueOf("#1B262C"), null}){
        @Override
        public String toString(){
            return "Lake";
        }
    },
    FERRY(ZoomLevels.zoomLevel1, new Color[]{Color.valueOf("#A1CAE2"), Color.valueOf("#03506F"), null}),
    UNKNOWN(ZoomLevels.zoomLevel5, new Color[]{Color.valueOf("#C8C2BC"), null, null}),
    BUILDING(ZoomLevels.zoomLevel5, new Color[3], new Color[]{Color.valueOf("#C8C2BC"), Color.valueOf("#1D3E53"), null}),
    TRACK(ZoomLevels.zoomLevel6, new Color[]{Color.GRAY, null, null}),
    PATH(ZoomLevels.zoomLevel6, new Color[]{Color.BISQUE, null, null}),
    UNCLASSIFIEDHIGHWAY(ZoomLevels.zoomLevel5, new Color[]{Color.LIGHTGRAY, null, null}) {
        @Override
        public boolean supportsTransportBy(TravelType travelType) {
            switch (travelType) {
                case CAR:
                    return true;
                case FOOT:
                    return true;
                case BICYCLE:
                    return true;
                default:
                    throw new UnsupportedTravelTypeException(travelType);
            }
        }
    }, 
    MOTORWAY(ZoomLevels.zoomLevel0, new Color[]{Color.valueOf("#E892A2"), null, null}) {
        @Override
        public boolean supportsTransportBy(TravelType travelType) {
            switch (travelType) {
                case CAR:
                    return true;
                case FOOT:
                    return false;
                case BICYCLE:
                    return false;
                default:
                    throw new UnsupportedTravelTypeException(travelType);
            }
        }
    },
    TRUNK(ZoomLevels.zoomLevel3, new Color[]{Color.valueOf("#F9B29C"), null, null}) {
        @Override
        public boolean supportsTransportBy(TravelType travelType) {
            switch (travelType) {
                case CAR:
                    return true;
                case FOOT:
                    return false;
                case BICYCLE:
                    return false;
                default:
                    throw new UnsupportedTravelTypeException(travelType);
            }
        }
    },
    PRIMARY(ZoomLevels.zoomLevel3, new Color[]{Color.valueOf("#F9B29C"), null, null}) {
        @Override
        public boolean supportsTransportBy(TravelType travelType) {
            switch (travelType) {
                case CAR:
                    return true;
                case FOOT:
                    return false;
                case BICYCLE:
                    return false;
                default:
                    throw new UnsupportedTravelTypeException(travelType);
            }
        }
    },
    SECONDARY(ZoomLevels.zoomLevel4, new Color[]{Color.valueOf("#C3943F"), null, null}) {
        @Override
        public boolean supportsTransportBy(TravelType travelType) {
            switch (travelType) {
                case CAR:
                    return true;
                case FOOT:
                    return false;
                case BICYCLE:
                    return false;
                default:
                    throw new UnsupportedTravelTypeException(travelType);
            }
        }
    },
    TERTIARY(ZoomLevels.zoomLevel4, new Color[]{Color.valueOf("#76830E"), Color.WHITE, null}) {
        @Override
        public boolean supportsTransportBy(TravelType travelType) {
            switch (travelType) {
                case CAR:
                    return true;
                case FOOT:
                    return true;
                case BICYCLE:
                    return true;
                default:
                    throw new UnsupportedTravelTypeException(travelType);
            }
        }
    },
    RESIDENTIALROAD(ZoomLevels.zoomLevel5, new Color[]{Color.LIGHTGREY, Color.GREY, null}) {
        @Override
        public boolean supportsTransportBy(TravelType travelType) {
            switch (travelType) {
                case CAR:
                    return true;
                case FOOT:
                    return true;
                case BICYCLE:
                    return true;
                default:
                    throw new UnsupportedTravelTypeException(travelType);
            }
        }
    },
    SERVICEROAD(ZoomLevels.zoomLevel5, new Color[]{Color.WHITE, Color.DARKGREY, null}) {
        @Override
        public boolean supportsTransportBy(TravelType travelType) {
            switch (travelType) {
                case CAR:
                    return true;
                case FOOT:
                    return true;
                case BICYCLE:
                    return true;
                default:
                    throw new UnsupportedTravelTypeException(travelType);
            }
        }
    },
    CYCLEWAY(ZoomLevels.zoomLevel5, new Color[]{Color.YELLOW, null, null}){
        @Override
        public boolean supportsTransportBy(TravelType travelType){
            switch (travelType) {
                case CAR:
                    return false;
                case FOOT:
                    return true;
                case BICYCLE:
                    return true;
                default:
                    throw new UnsupportedTravelTypeException(travelType);
            }  
        }
    }

    ;
    private Paint[] edgePaints;
    private Paint[] fillPaints;
    private double zoomLevel;
    private WayType(double zoomLevel, Paint[] edgePaints) {
        this.zoomLevel = zoomLevel;
        this.edgePaints = new Paint[3];
        fillPaints = null;
        for (int i = 0; i < 3; i++) {
            this.edgePaints[i] = edgePaints[i];
        }
    }
    private WayType(double zoomLevel, Paint[] edgePaints, Paint[] fillPaints) {
        this(zoomLevel, edgePaints);
        this.fillPaints = new Paint[3];
        for (int i = 0; i < 3; i++) {
            this.fillPaints[i] = fillPaints[i];
        }
    }
    public Paint getEdgePaint(Theme theme) {
        switch (theme) {
            case LIGHT:
            case DARK:
                if (edgePaints[theme.ordinal()] == null)
                    return edgePaints[0];
                else
                    return edgePaints[theme.ordinal()];
            case EPILEPSY:
                return Color.rgb((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255));
            default:
                System.out.println("Unknown theme!");
                return edgePaints[0];
        }
    }
    public Paint getFillPaint(Theme theme) {
        switch (theme) {
            case LIGHT:
            case DARK:
                if (fillPaints[theme.ordinal()] == null)
                    return fillPaints[0];
                else
                    return fillPaints[theme.ordinal()];
            case EPILEPSY:
                return Color.rgb((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255));
            default:
                System.out.println("Unknown theme!");
                return fillPaints[0];
        }
    }
    public boolean shouldFill() {
        return fillPaints != null;
    }
    public boolean visibleByZoomLevel(double currentZoom) {
        return currentZoom < zoomLevel;
    }
    public boolean supportsTransportBy(TravelType travelType) {
        return false;
    }

    public static Set<WayType> roadKeySet() {
        Set<WayType> set = new HashSet<>();

        set.add(WayType.PRIMARY);
        set.add(WayType.SECONDARY);
        set.add(WayType.TERTIARY);
        set.add(WayType.UNCLASSIFIEDHIGHWAY);
        set.add(WayType.RESIDENTIALROAD);
        set.add(WayType.SERVICEROAD);
        set.add(WayType.FERRY);
        set.add(WayType.MOTORWAY);
        set.add(WayType.TRUNK);
        set.add(WayType.CYCLEWAY);

        return set;
    }
}

package bfst22.vector.model.KdTree;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import bfst22.vector.model.OSMNode;
import bfst22.vector.model.WayType;
import bfst22.vector.model.drawable.Drawable;

//Class for the parent of the leaves in kdtree, becuase it's children are list and not points
public class FinalPoint extends Point{
    public static final long serialVersionUID = 9089217;
    float value;
    EnumMap<WayType, List<Drawable>> drawablesRightChild = new EnumMap<>(WayType.class); {
        for (var type : WayType.values()) drawablesRightChild.put(type, new ArrayList<Drawable>());
    }
    EnumMap<WayType, List<Drawable>> drawablesLeftChild = new EnumMap<>(WayType.class); {
        for (var type : WayType.values()) drawablesLeftChild.put(type, new ArrayList<Drawable>());
    }
    
    public FinalPoint(List<OSMNode> subList, boolean CompareX) {
        super(CompareX ? subList.get(subList.size()/2).getLongitude() : subList.get(subList.size()/2).getLatitude());
    }

    @Override
    public Point getLeftChild() {
        return null;
    }
    @Override
    public Point getRightChild() {
        return null;
    }

    public EnumMap<WayType, List<Drawable>> getDrawablesLeftChild(){
        return drawablesLeftChild;
    }
    public EnumMap<WayType, List<Drawable>> getDrawablesRightChild(){
        return drawablesRightChild;
    }

}

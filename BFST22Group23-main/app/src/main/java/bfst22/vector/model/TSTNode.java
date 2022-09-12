package bfst22.vector.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class TSTNode implements Serializable {
    private Character character;
    private boolean finalNode;
    TSTNode left;
    TSTNode center;
    TSTNode right;
    HashMap<String, OSMNode> map;
    ArrayList<String> cities;

    public Character getCharacter(){
        return character;
    }

    public TSTNode getLeftTSTNode(){
        return left;
    }

    public TSTNode getCenterTSTNode(){
        return center;
    }

    public TSTNode getRightTSTNode(){
        return right;
    }

    public void setFinalNode(boolean value){
        finalNode = value;
    }

    public boolean getFinalNode(){
        return finalNode;
    }

    public TSTNode(Character character) {
        this.character = character;
        this.left = null;
        this.center = null;
        this.right = null;
        this.finalNode = false;
        this.map = new HashMap<>();
        this.cities = new ArrayList<>();
    }
}

package bfst22.vector.model;

import java.io.Serializable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TST implements Serializable{
    private TSTNode root;
    private ObservableList<String> list = FXCollections.observableArrayList();
    private ObservableList<String> list2 = FXCollections.observableArrayList();

    public TST(){
        this.root = null;
    }
    public TST(TSTNode root){
        this.root = root;
    }

    public TSTNode getRoot(){
        return root;
    }

    public OSMNode get(String key) {
        TSTNode node = get(root, key, 0);
        if (node == null) return null;
        var word = key.split(", ");
        return (OSMNode) node.map.get(word[2]);
    }
    
    private TSTNode get(TSTNode node, String key, int d) {
        if (node == null) return null;
        var word = key.split(", ");
        var address = word[0];
        char c = address.charAt(d);
        if (c < node.getCharacter()) return get(node.getLeftTSTNode(), key, d);
        else if (c > node.getCharacter()) return get(node.getRightTSTNode(), key, d);
        else if (d < address.length() - 1) return get(node.getCenterTSTNode(), key, d+1);
        else return node;
    }

    public void put(String key, OSMNode val) {
        root = put(root, key, val, 0); 
    }

    private TSTNode put(TSTNode node, String key, OSMNode val, int d) {
        var word = key.split(", ");
        var address = word[0];
        char c = address.charAt(d);
        if (node == null) { 
            node = new TSTNode(c); 
        }
        if (c < node.getCharacter()) node.left = put(node.getLeftTSTNode(), key, val, d);
        else if (c > node.getCharacter()) node.right = put(node.getRightTSTNode(), key, val, d);
        else if (d < address.length() - 1) node.center = put(node.getCenterTSTNode(), key, val, d+1);
        else {
            node.map.put(word[2].intern(), val);
            node.cities.add(word[2].intern());
            node.setFinalNode(true);
        }
        return node;
    }

    public boolean contains(String key) {
        if (key == null) {
            throw new IllegalArgumentException("argument to contains() is null");
        }
        return get(key) != null;
    }

    public Iterable<String> keysWithPrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("calls keysWithPrefix() with null argument");
        }
        Queue<String> queue = new Queue<String>();
        TSTNode node = get(root, prefix, 0);
        if (node == null) return queue;
        if (node.getFinalNode()) queue.enqueue(prefix);
        collect(node.center, new StringBuilder(prefix), queue);
        return queue;
    }

    private void collect(TSTNode node, StringBuilder prefix, Queue<String> queue) {
        if (node == null) return;
        collect(node.left,  prefix, queue);
        if (node.getFinalNode()) {
            for (String string : node.cities) {
                queue.enqueue(prefix.toString().toUpperCase() + node.getCharacter() + ", " + string.toUpperCase());
            }
        }
        collect(node.center, prefix.append(node.getCharacter()), queue);
        prefix.deleteCharAt(prefix.length() - 1);
        collect(node.right, prefix, queue);
    }

    public ObservableList<String> search(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        } 
        var possibleMatches = keysWithPrefix(query);
        for (String string : possibleMatches) {
            list.add(string);
        }  
        return list;
    }

    public ObservableList<String> search2(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        } 
        var possibleMatches = keysWithPrefix(query);
        for (String string : possibleMatches) {
            list2.add(string);
        }  
        return list2;
    }
}

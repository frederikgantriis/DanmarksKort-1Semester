package bfst22.vector.model.ShortesPath;

import java.io.Serializable;
import java.util.ArrayList;

public class RoadNode implements Serializable {
    public static final long serialVersionUID = 579564;
    private long id;
    private ArrayList<Edge> edges;

    public RoadNode(long id){
        this.id = id;
        edges = new ArrayList<>();
    }

    public final void addEdge(Edge edge){
        edges.add(edge);
    }

    public final long getId() {
        return id;
    }
    public final ArrayList<Edge> getEdges(){
        return (ArrayList<Edge>) edges;
    }

    public final void setId(long id) {
        this.id = id;
    }
}

package bfst22.vector.model.ShortesPath;

import java.util.ArrayList;
import java.util.List;

public class IndexMinPQ {
    private List<RoadNode> node_pq;
    private List<Double> weight_pq;
    private int n;

    public IndexMinPQ() {
        node_pq = new ArrayList<>();
        weight_pq = new ArrayList<>();
        node_pq.add(null);
        weight_pq.add(null);
        n = 0;
    }

    private boolean less(int i, int j) {
        return weight_pq.get(i) < weight_pq.get(j);
    }

    private void exchange(int i, int j) {
        // Save first in temporary variable
        RoadNode tmp_node = node_pq.get(i);
        double tmp_weight = weight_pq.get(i);

        // Change first element to second
        node_pq.set(i, node_pq.get(j));
        weight_pq.set(i, weight_pq.get(j));

        // Change second element to temporary (first)
        node_pq.set(j, tmp_node);
        weight_pq.set(j, tmp_weight);
    }

    private void swim(int k) {
        while (k > 1 && less(k, k/2)) {
            exchange(k, k/2);
            k = k/2;
        }
    }

    private void sink(int k) {
        while (2*k <= n) {
            int j = 2*k;
            if (j < n && less(j+1, j)) j++;
            if (!less(j, k)) break;
            exchange(j, k);
            k = j;
        }
    }

    public int size() {
        return n;
    }

    public void insert(RoadNode startNode, double weight) {
        n++;
        node_pq.add(startNode);
        weight_pq.add(weight);
        swim(n);
    }

    public RoadNode deleteMinimum() {
        if (n < 1) {
            return null;
        }
        RoadNode min = node_pq.get(1);
        exchange(1, n);
        node_pq.remove(n);
        weight_pq.remove(n);
        n--;
        sink(1);
        return min;
    }

    public boolean isEmpty() {
        return size() < 1;
    }

    public boolean contains(RoadNode destination) {
        return node_pq.contains(destination);
    }

    public void change(RoadNode toNode, double newDistance) {
        int oldIndex = node_pq.indexOf(toNode);
        double oldWeight = weight_pq.get(oldIndex);
        weight_pq.set(oldIndex, newDistance);
        if (newDistance > oldWeight) sink(oldIndex);
        else swim(oldIndex);
    }
}

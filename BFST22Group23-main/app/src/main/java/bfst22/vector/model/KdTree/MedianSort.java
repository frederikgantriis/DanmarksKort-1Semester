package bfst22.vector.model.KdTree;

import java.util.Comparator;
import java.util.List;

import bfst22.vector.model.OSMNode;

public class MedianSort {
    public static void medianSort(List<OSMNode> osmNodes, Comparator<OSMNode> comparator){
        int lo = 0, hi = osmNodes.size() - 1, k =osmNodes.size()/2;
        while (hi > lo){
            int j = partition(osmNodes, lo, hi, comparator);
            if (j == k) break;
            else if (j > k) hi = j - 1;
            else if (j < k) lo = j + 1;
        } 
    }
    private static int partition(List<OSMNode> osmNodes, int lo, int hi, Comparator<OSMNode> comparator){
        int i = lo, j = hi+1;
        OSMNode v = osmNodes.get(lo); // partitioning item
        while (true){ // Scan right, scan left, check for scan complete, and exchange.
            while (-1 == comparator.compare(osmNodes.get(++i), v)) if (i == hi) break;
            while (-1 == comparator.compare(v, osmNodes.get(--j))) if (j == lo) break;
            if (i >= j) break;
            exch(osmNodes, i, j);
        }
        exch(osmNodes, lo, j);
        return j;
    }
    private static void exch(List<OSMNode> osmNodes, int i, int j) {
        OSMNode temp = osmNodes.get(i);
        osmNodes.set(i, osmNodes.get(j));
        osmNodes.set(j, temp);
    }
}

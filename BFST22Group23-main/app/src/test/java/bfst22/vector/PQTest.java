package bfst22.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import bfst22.vector.model.ShortesPath.IndexMinPQ;
import bfst22.vector.model.ShortesPath.RoadNode;

class PQTest {
    @Test void deleteMin() {
        IndexMinPQ pq = new IndexMinPQ();
        RoadNode node1 = new RoadNode(123);
        RoadNode node2 = new RoadNode(456);
        RoadNode node3 = new RoadNode(789);

        pq.insert(node1, 3);
        pq.insert(node2, 4);
        pq.insert(node3, 2);

        assertEquals(node3, pq.deleteMinimum());
    }

    @Test void deleteMin2() {
        IndexMinPQ pq = new IndexMinPQ();
        RoadNode node1 = new RoadNode(123);
        RoadNode node2 = new RoadNode(456);
        RoadNode node3 = new RoadNode(789);

        pq.insert(node1, 3);
        pq.insert(node2, 4);
        pq.insert(node3, 2);

        assertEquals(node3, pq.deleteMinimum());
        assertEquals(node1, pq.deleteMinimum());
        assertEquals(node2, pq.deleteMinimum());
        assertEquals(null, pq.deleteMinimum());
    }

    @Test void deleteMinEmpty() {
        IndexMinPQ pq = new IndexMinPQ();

        assertEquals(null, pq.deleteMinimum());
    }

    @Test void containsNode() {
        IndexMinPQ pq = new IndexMinPQ();

        RoadNode node1 = new RoadNode(123);
        RoadNode node2 = new RoadNode(456);
        RoadNode node3 = new RoadNode(789);

        pq.insert(node1, 3);
        pq.insert(node2, 4);
        
        assertEquals(true, pq.contains(node1));
        assertEquals(true, pq.contains(node2));
        assertEquals(false, pq.contains(node3));
    }

    @Test void isEmptyNew() {
        IndexMinPQ pq = new IndexMinPQ();

        assertEquals(true, pq.isEmpty());
    }

    @Test void isEmptyFalse() {
        IndexMinPQ pq = new IndexMinPQ();
        
        RoadNode node1 = new RoadNode(123);
        RoadNode node2 = new RoadNode(456);
        RoadNode node3 = new RoadNode(789);

        pq.insert(node1, 3);
        pq.insert(node2, 4);
        pq.insert(node3, 2);

        assertEquals(false, pq.isEmpty());
    }

    @Test void isEmptyEmptied() {
        IndexMinPQ pq = new IndexMinPQ();
        
        RoadNode node1 = new RoadNode(123);
        RoadNode node2 = new RoadNode(456);
        RoadNode node3 = new RoadNode(789);

        pq.insert(node1, 3);
        pq.insert(node2, 4);
        pq.insert(node3, 2);

        pq.deleteMinimum();
        pq.deleteMinimum();
        pq.deleteMinimum();

        assertEquals(true, pq.isEmpty());
    }

    @Test void isEmptyOneObject() {
        IndexMinPQ pq = new IndexMinPQ();

        RoadNode node1 = new RoadNode(123);

        pq.insert(node1, 3);

        assertEquals(false, pq.isEmpty());
    }
}


package bfst22.vector.model.KdTree;

public class KDTreeQueue {
    public class Split {
        private float value;
        private boolean isX;
        private Split next;

        public Split(float value, boolean isX) {
            this.value = value;
            this.isX = isX;
            this.next = null;
        }
        
        public float getValue() {
            return value;
        }
        public boolean isX() {
            return isX;
        }
        public Split getNext() {
            return next;
        }
        public void setNext(Split next) {
            this.next = next;
        }
    }
    private Split first;
    private Split last;
    private int size;

    public KDTreeQueue() {
        first = null;
        size = 0;
    }

    public void enqueue(float value, boolean isX) {
        size++;
        if (first == null) {
            first = new Split(value, isX);
            last = first;
            return;
        }

        last.setNext(new Split(value, isX));
        last = last.getNext();
    }

    public Split dequeue() {
        if (size <= 0) {
            throw new NullPointerException();
        }
        Split output = first;
        first = first.getNext();
        if (first == null)
            last = null;
        size--;
        return output;
    }

    public Split peek() {
        return first;
    }

    public Split doublePeek() {
        return first.getNext();
    }

    public int size() {
        return size;
    }
}
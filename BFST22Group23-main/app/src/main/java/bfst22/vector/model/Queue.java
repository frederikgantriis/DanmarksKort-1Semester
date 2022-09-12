package bfst22.vector.model;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Queue<Item> implements Iterable<Item> {
    private Node<Item> first;    
    private Node<Item> last;     
    
    private static class Node<Item> {
        private Item item;
        private Node<Item> next;
    }
    
    public Queue() {
        first = null;
        last  = null;
    }
    
    public boolean isEmpty() {
        return first == null;
    }
    
    public void enqueue(Item item) {
        Node<Item> oldlast = last;
        last = new Node<Item>();
        last.item = item;
        last.next = null;
        if (isEmpty()) first = last;
        else           oldlast.next = last;
    }

    public Iterator<Item> iterator()  {
        return new LinkedIterator(first);  
    }

    private class LinkedIterator implements Iterator<Item> {
        private Node<Item> current;

        public LinkedIterator(Node<Item> first) {
            current = first;
        }

        public boolean hasNext()  { return current != null;                     }
        public void remove()      { throw new UnsupportedOperationException();  }

        public Item next() {
            if (!hasNext()) throw new NoSuchElementException();
            Item item = current.item;
            current = current.next; 
            return item;
        }
    }
}


package nl.han.ica.datastructures;

import java.util.ArrayList;
import java.util.Arrays;

public class HANQueue<T> implements IHANQueue<T> {

    private final ArrayList<T> queue;

    // Make empty queue
    public HANQueue() {
        this.queue = new ArrayList<>();
    }

    // Make queue with items
    public HANQueue(ArrayList<T> items) {
        this.queue = items;
    }

    @Override
    public void clear() {
        this.queue.clear();
    }

    @Override
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    @Override
    public void enqueue(T value) {
        this.queue.add(value);
    }

    @Override
    public T dequeue() {
        return this.queue.removeFirst();
    }

    @Override
    public T peek() {
        return this.queue.getFirst();
    }

    @Override
    public int getSize() {
        return this.queue.size();
    }

    public String toString() {
        return Arrays.toString(queue.toArray());
    }
}

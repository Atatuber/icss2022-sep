package nl.han.ica.datastructures;

import java.util.ArrayList;
import java.util.Arrays;

public class HANStack<T> implements IHANStack<T> {

    private final ArrayList<T> stack;

    // Make empty stack
    public HANStack() {
        this.stack = new ArrayList<>();
    }

    // Make stack with items
    public HANStack(ArrayList<T> items) {
        this.stack = items;
    }

    @Override
    public void push(T value) {
        this.stack.add(value);
    }

    @Override
    public T pop() {
        return this.stack.removeLast();
    }

    @Override
    public T peek() {
        return this.stack.getLast();
    }
    public String toString() {
        return Arrays.toString(this.stack.toArray());
    }
}

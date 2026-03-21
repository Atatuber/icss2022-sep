package nl.han.ica.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HANStack<T> implements IHANStack<T> {

    private final List<T> stack;

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
        return this.stack.remove(stack.toArray().length - 1);
    }

    @Override
    public T peek() {
        return this.stack.get(stack.toArray().length - 1);
    }

    public String toString() {
        return Arrays.toString(this.stack.toArray());
    }
}

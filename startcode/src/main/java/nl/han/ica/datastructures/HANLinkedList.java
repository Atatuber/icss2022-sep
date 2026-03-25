package nl.han.ica.datastructures;

import java.util.Arrays;

public class HANLinkedList<T> implements IHANLinkedList<T> {

    private Object[] list;
    private int size;

    public HANLinkedList() {
        list = new Object[0];
        size = 0;
    }

    @Override
    public void addFirst(T value) {
        Object[] copy = new Object[size + 1];
        copy[0] = value;

        for (int i = 0; i < size; i++) {
            copy[i + 1] = list[i];
        }

        list = copy;
        size++;
    }

    @Override
    public void clear() {
        list = new Object[0];
        size = 0;
    }

    @Override
    public void insert(int index, T value) {
        checkBounds(index);
        Object[] copy = new Object[size + 1];

        for (int i = 0; i < index; i++) {
            copy[i + 1] = list[i];
        }

        copy[index] = value;

        for (int i = index; i < size; i++) {
            copy[i] = list[i];
        }
        list = copy;
        size++;
    }

    @Override
    public void delete(int pos) {
        checkBounds(pos);
        Object[] copy = new Object[size - 1];
        int copyIndex = 0;
        for (int i = 0; i < size; i++) {
            if (i == pos) {
                continue;
            }
            copy[copyIndex] = list[i];
            copyIndex++;
        }
        list = copy;
        size--;
    }

    @Override
    public T get(int pos) {
        checkBounds(pos);
        return (T) list[pos];
    }

    @Override
    public void removeFirst() {
        if(size == 0) throw new RuntimeException("Empty LinkedList.");

        Object[] copy = new Object[size - 1];
        for (int i = 1; i < size; i++) {
            copy[i - 1] = list[i];
        }
        list = copy;
        size--;
    }

    @Override
    public T getFirst() {
        return (T) list[0];
    }

    @Override
    public int getSize() {
        return size;
    }

    private void checkBounds(int value) {
        if (value < 0 || value >= size) {
            throw new IndexOutOfBoundsException();
        }
    }

    public String toString() {
        return Arrays.toString(list);
    }
}

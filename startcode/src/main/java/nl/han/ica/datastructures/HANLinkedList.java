package nl.han.ica.datastructures;

import java.util.Arrays;

public class HANLinkedList<T> implements IHANLinkedList<T> {

    private Node[] list;
    private int size;

    private static class Node<T> {
        private final T value;
        private final T left;
        private final T right;

        public Node(T value) {
            this.value = value;
            this.left = null;
            this.right = null;
        }
        public Node(T value, T left, T right) {
            this.value = value;
            this.left = left;
            this.right = right;
        }
        public String toString() {
            assert left != null;
            assert right != null;
            return value + " LEFT: " + left + ", RIGHT: " + right;
        }
    }

    // Make a LinkedList with 0 items
    public HANLinkedList() {
        list = new Node[0];
        size = 0;
    }

    // Make a LinkedList with specified items
    public HANLinkedList(T[] items) {
        Node[] temp = new Node[items.length];

        for(int i = 0; i < items.length; i++) {
            Node node = new Node(items[i]);
            temp[i] = node;
        }

        list = temp;
        size = items.length;
    }

    @Override
    public void addFirst(T value) {
        Node[] copy = new Node[size + 1];
        copy[0] = new Node(value);

        for (int i = 0; i < size; i++) {
            Node node = new Node(list[i]);
            copy[i + 1] = node;
        }



        list = copy;
        size++;
    }

    @Override
    public void clear() {
        list = new Node[0];
    }

    @Override
    public void insert(int index, T value) {
        checkBounds(index);
        Node[] copy = new Node[size + 1];

        for (int i = 0; i < index; i++) {
            copy[i] = list[i];
        }

        Node node = new Node(value);
        copy[index] = node;

        for (int i = index; i < size; i++) {
            copy[i] = list[i];
        }
        list = copy;
        size++;
    }

    @Override
    public void delete(int pos) {
        checkBounds(pos);
        Node[] copy = new Node[size - 1];
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
        Node[] copy = new Node[size - 1];
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
        if (value < 0 || value > size) {
            throw new IndexOutOfBoundsException();
        }
    }

    public String toString() {
        return Arrays.toString(list);
    }
}

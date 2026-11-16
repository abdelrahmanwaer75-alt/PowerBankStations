package edu.powerbank.linkedlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomLinkedList<T> {
    private ListNode<T> head;
    private ListNode<T> tail;
    private int size;

    public void addFirst(T value) {
        ListNode<T> node = new ListNode<>(value);
        node.next = head;
        head = node;
        if (tail == null) {
            tail = node;
        }
        size++;
    }

    public void addLast(T value) {
        ListNode<T> node = new ListNode<>(value);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public void add(T value) {
        addLast(value);
    }

    public void insertAt(int index, T value) {
        checkInsertIndex(index);
        if (index == 0) {
            addFirst(value);
            return;
        }
        if (index == size) {
            addLast(value);
            return;
        }
        ListNode<T> previous = nodeAt(index - 1);
        ListNode<T> node = new ListNode<>(value);
        node.next = previous.next;
        previous.next = node;
        size++;
    }

    public boolean remove(T value) {
        ListNode<T> previous = null;
        ListNode<T> current = head;
        while (current != null) {
            if (Objects.equals(current.value, value)) {
                unlink(previous, current);
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    public T removeAt(int index) {
        checkElementIndex(index);
        ListNode<T> previous = index == 0 ? null : nodeAt(index - 1);
        ListNode<T> current = previous == null ? head : previous.next;
        T value = current.value;
        unlink(previous, current);
        return value;
    }

    public int search(T value) {
        int index = 0;
        for (ListNode<T> current = head; current != null; current = current.next) {
            if (Objects.equals(current.value, value)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public T get(int index) {
        checkElementIndex(index);
        return nodeAt(index).value;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public List<T> toList() {
        List<T> values = new ArrayList<>(size);
        for (ListNode<T> current = head; current != null; current = current.next) {
            values.add(current.value);
        }
        return values;
    }

    public String display() {
        return toList().toString();
    }

    private void unlink(ListNode<T> previous, ListNode<T> current) {
        if (previous == null) {
            head = current.next;
        } else {
            previous.next = current.next;
        }
        if (current == tail) {
            tail = previous;
        }
        size--;
        if (size == 0) {
            head = tail = null;
        }
    }

    private ListNode<T> nodeAt(int index) {
        ListNode<T> current = head;
        for (int position = 0; position < index; position++) {
            current = current.next;
        }
        return current;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }

    private void checkInsertIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }
}

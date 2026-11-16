package edu.powerbank.queue;

public final class QueueNode<T> {
    final T value;
    QueueNode<T> next;

    QueueNode(T value) {
        this.value = value;
    }
}

package edu.powerbank;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import edu.powerbank.bst.BinarySearchTree;
import edu.powerbank.linkedlist.CustomLinkedList;
import edu.powerbank.queue.CustomQueue;
import edu.powerbank.sorting.BubbleSort;
import edu.powerbank.sorting.InsertionSort;
import edu.powerbank.sorting.MergeSort;
import edu.powerbank.sorting.QuickSort;
import edu.powerbank.sorting.SelectionSort;

class StructuresTest {
    @Test
    void bstSupportsTraversalDuplicatesAndDeletion() {
        var tree = new BinarySearchTree<Integer>();
        assertTrue(tree.isEmpty());
        assertThrows(java.util.NoSuchElementException.class, tree::findMin);
        tree.insert(4);
        tree.insert(2);
        tree.insert(6);
        tree.insert(4);
        assertEquals(2, tree.search(2));
        assertNull(tree.search(10));
        assertEquals(2, tree.findMin());
        assertEquals(6, tree.findMax());
        assertEquals(2, tree.height());
        assertEquals(java.util.List.of(2, 4, 6), tree.inorderTraversal());
        assertEquals(java.util.List.of(4, 2, 6), tree.preorderTraversal());
        assertEquals(java.util.List.of(2, 6, 4), tree.postorderTraversal());
        tree.delete(4);
        tree.delete(100);
        assertEquals(java.util.List.of(2, 6), tree.inorder());
        tree.delete(2);
        tree.delete(6);
        assertTrue(tree.isEmpty());
    }

    @Test
    void queuePreservesFifoAndCanBeReusedAfterClear() {
        var queue = new CustomQueue<Integer>();
        assertTrue(queue.isEmpty());
        assertThrows(java.util.NoSuchElementException.class, queue::peek);
        assertThrows(java.util.NoSuchElementException.class, queue::dequeue);
        queue.enqueue(1);
        queue.enqueue(2);
        assertEquals(1, queue.peek());
        assertEquals(1, queue.dequeue());
        assertEquals(1, queue.size());
        queue.clear();
        assertTrue(queue.isEmpty());
        queue.enqueue(3);
        assertEquals(3, queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    void linkedListHandlesIndexAndTailOperations() {
        var list = new CustomLinkedList<String>();
        assertTrue(list.isEmpty());
        list.addFirst("b");
        list.insertAt(0, "a");
        list.insertAt(2, "d");
        list.insertAt(2, "c");
        assertEquals(java.util.List.of("a", "b", "c", "d"), list.toList());
        assertEquals(2, list.search("c"));
        assertEquals("a", list.removeAt(0));
        assertEquals("d", list.removeAt(list.size() - 1));
        assertTrue(list.remove("b"));
        assertFalse(list.remove("missing"));
        assertTrue(list.remove("c"));
        assertTrue(list.isEmpty());
        list.addLast("reused");
        assertEquals("reused", list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.insertAt(2, "bad"));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    }

    @Test
    void allSortsHandleEmptySingletonDuplicatesAndReverseOrder() {
        assertSorts(new Integer[] {});
        assertSorts(new Integer[] { 1 });
        assertSorts(new Integer[] { 3, 1, 3, 2, 1 });
        assertSorts(new Integer[] { 5, 4, 3, 2, 1 });
        assertSorts(new Integer[] { 1, 2, 3, 4, 5 });
    }

    private void assertSorts(Integer[] input) {
        Integer[] expected = input.clone();
        java.util.Arrays.sort(expected);
        assertSorted(expected, input, BubbleSort::sort);
        assertSorted(expected, input, SelectionSort::sort);
        assertSorted(expected, input, InsertionSort::sort);
        assertSorted(expected, input, MergeSort::sort);
        assertSorted(expected, input, QuickSort::sort);
    }

    private void assertSorted(
            Integer[] expected, Integer[] input, java.util.function.Consumer<Integer[]> sort) {
        Integer[] actual = input.clone();
        sort.accept(actual);
        assertArrayEquals(expected, actual);
    }
}

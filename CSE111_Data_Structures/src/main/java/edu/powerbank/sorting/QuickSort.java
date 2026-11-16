package edu.powerbank.sorting;

public final class QuickSort {
    private QuickSort() {
    }

    public static <T extends Comparable<? super T>> void sort(T[] values) {
        quickSort(values, 0, values.length - 1);
    }

    private static <T extends Comparable<? super T>> void quickSort(
            T[] values, int low, int high) {
        while (low < high) {
            int[] equalRange = partition(values, low, high);
            if (equalRange[0] - low < high - equalRange[1]) {
                quickSort(values, low, equalRange[0] - 1);
                low = equalRange[1] + 1;
            } else {
                quickSort(values, equalRange[1] + 1, high);
                high = equalRange[0] - 1;
            }
        }
    }

    private static <T extends Comparable<? super T>> int[] partition(
            T[] values, int low, int high) {
        T pivot = values[low + (high - low) / 2];
        int less = low;
        int current = low;
        int greater = high;
        while (current <= greater) {
            int comparison = values[current].compareTo(pivot);
            if (comparison < 0) {
                swap(values, less++, current++);
            } else if (comparison > 0) {
                swap(values, current, greater--);
            } else {
                current++;
            }
        }
        return new int[] { less, greater };
    }

    private static <T> void swap(T[] values, int first, int second) {
        T temporary = values[first];
        values[first] = values[second];
        values[second] = temporary;
    }
}

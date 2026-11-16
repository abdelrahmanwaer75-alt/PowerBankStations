package edu.powerbank.sorting;

public final class BubbleSort {
    private BubbleSort() {
    }

    public static <T extends Comparable<? super T>> void sort(T[] values) {
        for (int end = values.length - 1; end > 0; end--) {
            boolean swapped = false;
            for (int index = 0; index < end; index++) {
                if (values[index].compareTo(values[index + 1]) > 0) {
                    T temporary = values[index];
                    values[index] = values[index + 1];
                    values[index + 1] = temporary;
                    swapped = true;
                }
            }
            if (!swapped) {
                return;
            }
        }
    }
}

package edu.powerbank.sorting;

public final class SelectionSort {
    private SelectionSort() {
    }

    public static <T extends Comparable<? super T>> void sort(T[] values) {
        for (int start = 0; start < values.length - 1; start++) {
            int minimum = start;
            for (int index = start + 1; index < values.length; index++) {
                if (values[index].compareTo(values[minimum]) < 0) {
                    minimum = index;
                }
            }
            T temporary = values[start];
            values[start] = values[minimum];
            values[minimum] = temporary;
        }
    }
}

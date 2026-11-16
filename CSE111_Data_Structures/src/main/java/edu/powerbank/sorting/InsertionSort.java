package edu.powerbank.sorting;

public final class InsertionSort {
    private InsertionSort() {
    }

    public static <T extends Comparable<? super T>> void sort(T[] values) {
        for (int index = 1; index < values.length; index++) {
            T value = values[index];
            int position = index - 1;
            while (position >= 0 && values[position].compareTo(value) > 0) {
                values[position + 1] = values[position];
                position--;
            }
            values[position + 1] = value;
        }
    }
}

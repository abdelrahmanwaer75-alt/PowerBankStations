package edu.powerbank.sorting;

public final class MergeSort {
    private MergeSort() {
    }

    public static <T extends Comparable<? super T>> void sort(T[] values) {
        if (values.length < 2) {
            return;
        }
        @SuppressWarnings("unchecked")
        T[] temporary = (T[]) new Comparable<?>[values.length];
        sort(values, temporary, 0, values.length);
    }

    private static <T extends Comparable<? super T>> void sort(
            T[] values, T[] temporary, int start, int end) {
        if (end - start < 2) {
            return;
        }
        int middle = start + (end - start) / 2;
        sort(values, temporary, start, middle);
        sort(values, temporary, middle, end);

        int left = start;
        int right = middle;
        int output = start;
        while (left < middle && right < end) {
            temporary[output++] = values[left].compareTo(values[right]) <= 0
                    ? values[left++] : values[right++];
        }
        while (left < middle) {
            temporary[output++] = values[left++];
        }
        while (right < end) {
            temporary[output++] = values[right++];
        }
        System.arraycopy(temporary, start, values, start, end - start);
    }
}

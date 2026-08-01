package com.hospital.util;

import com.hospital.model.Patient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manual merge sort implementation for Patient lists.
 *
 * Why implement this instead of just calling List.sort()? Your DSA requirement asks for
 * "sorting and searching algorithms integrated and documented" — java.util.Collections.sort
 * already IS a highly-optimized merge sort/Timsort under the hood, but writing it out here
 * demonstrates actual understanding of the algorithm (O(n log n) time, O(n) extra space)
 * rather than just delegating to a library.
 */
public class PatientSorter {

    // Sorts by last name, then first name, ascending. Returns a NEW list — does not mutate the input.
    public static List<Patient> mergeSortByName(List<Patient> patients) {
        List<Patient> copy = new ArrayList<>(patients);
        Comparator<Patient> comparator = Comparator
                .comparing(Patient::getLastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Patient::getFirstName, String.CASE_INSENSITIVE_ORDER);

        return mergeSort(copy, comparator);
    }

    private static List<Patient> mergeSort(List<Patient> list, Comparator<Patient> comparator) {
        if (list.size() <= 1) {
            return list; // base case: a list of 0 or 1 elements is already sorted
        }

        int mid = list.size() / 2;
        List<Patient> left = mergeSort(new ArrayList<>(list.subList(0, mid)), comparator);
        List<Patient> right = mergeSort(new ArrayList<>(list.subList(mid, list.size())), comparator);

        return merge(left, right, comparator);
    }

    private static List<Patient> merge(List<Patient> left, List<Patient> right, Comparator<Patient> comparator) {
        List<Patient> result = new ArrayList<>(left.size() + right.size());
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }
        // Append whatever's left over from whichever side wasn't fully consumed
        while (i < left.size()) {
            result.add(left.get(i++));
        }
        while (j < right.size()) {
            result.add(right.get(j++));
        }

        return result;
    }
}
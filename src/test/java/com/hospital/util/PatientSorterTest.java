package com.hospital.util;

import com.hospital.model.Patient;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientSorterTest {

    @Test
    void mergeSortByName_shouldSortAlphabeticallyByLastNameThenFirstName() {
        Patient p1 = new Patient(1, "Bob", "Zebra", LocalDate.of(2000, 1, 1), "Other", null, null, null);
        Patient p2 = new Patient(2, "Alice", "Apple", LocalDate.of(2000, 1, 1), "Other", null, null, null);
        Patient p3 = new Patient(3, "Zack", "Mango", LocalDate.of(2000, 1, 1), "Other", null, null, null);

        List<Patient> sorted = PatientSorter.mergeSortByName(List.of(p1, p2, p3));

        assertEquals("Apple", sorted.get(0).getLastName());
        assertEquals("Mango", sorted.get(1).getLastName());
        assertEquals("Zebra", sorted.get(2).getLastName());
    }

    @Test
    void mergeSortByName_shouldBeCaseInsensitive() {
        Patient p1 = new Patient(1, "A", "banana", LocalDate.of(2000, 1, 1), "Other", null, null, null);
        Patient p2 = new Patient(2, "B", "Apple", LocalDate.of(2000, 1, 1), "Other", null, null, null);

        List<Patient> sorted = PatientSorter.mergeSortByName(List.of(p1, p2));

        assertEquals("Apple", sorted.get(0).getLastName());
        assertEquals("banana", sorted.get(1).getLastName());
    }

    @Test
    void mergeSortByName_shouldNotMutateOriginalList() {
        Patient p1 = new Patient(1, "A", "Zebra", LocalDate.of(2000, 1, 1), "Other", null, null, null);
        Patient p2 = new Patient(2, "B", "Apple", LocalDate.of(2000, 1, 1), "Other", null, null, null);
        List<Patient> original = List.of(p1, p2); // immutable list — would throw if sort tried to mutate it in place

        List<Patient> sorted = PatientSorter.mergeSortByName(original);

        assertEquals("Zebra", original.get(0).getLastName()); // original order unchanged
        assertEquals("Apple", sorted.get(0).getLastName());   // returned list IS sorted
    }

    @Test
    void mergeSortByName_shouldHandleEmptyAndSingleElementLists() {
        assertTrue(PatientSorter.mergeSortByName(List.of()).isEmpty());

        Patient p1 = new Patient(1, "A", "Solo", LocalDate.of(2000, 1, 1), "Other", null, null, null);
        List<Patient> singleResult = PatientSorter.mergeSortByName(List.of(p1));

        assertEquals(1, singleResult.size());
        assertEquals("Solo", singleResult.get(0).getLastName());
    }
}
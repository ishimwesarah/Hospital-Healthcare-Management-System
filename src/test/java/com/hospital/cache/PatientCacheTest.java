package com.hospital.cache;

import com.hospital.model.Patient;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientCacheTest {

    @Test
    void get_shouldReturnEmptyOptional_whenPatientNotCached() {
        PatientCache cache = new PatientCache();

        assertTrue(cache.get(999).isEmpty());
    }

    @Test
    void put_thenGet_shouldReturnTheSamePatient() {
        PatientCache cache = new PatientCache();
        Patient patient = new Patient(1, "Test", "Patient", LocalDate.of(2000, 1, 1),
                "Other", "0788000000", "test@example.com", "Kigali");

        cache.put(patient);
        Patient fetched = cache.get(1).orElse(null);

        assertNotNull(fetched);
        assertEquals("Test", fetched.getFirstName());
    }

    @Test
    void putAll_shouldMarkCacheAsFullyLoaded() {
        PatientCache cache = new PatientCache();
        Patient p1 = new Patient(1, "A", "A", LocalDate.of(2000, 1, 1), "Other", null, null, null);
        Patient p2 = new Patient(2, "B", "B", LocalDate.of(2000, 1, 1), "Other", null, null, null);

        assertFalse(cache.isFullyLoaded());
        cache.putAll(List.of(p1, p2));

        assertTrue(cache.isFullyLoaded());
        assertEquals(2, cache.size());
    }

    @Test
    void invalidate_shouldClearCacheAndResetFullyLoadedFlag() {
        PatientCache cache = new PatientCache();
        Patient p1 = new Patient(1, "A", "A", LocalDate.of(2000, 1, 1), "Other", null, null, null);
        cache.putAll(List.of(p1));

        cache.invalidate();

        assertFalse(cache.isFullyLoaded());
        assertEquals(0, cache.size());
        assertTrue(cache.get(1).isEmpty());
    }
}
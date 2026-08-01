package com.hospital.cache;

import com.hospital.model.Patient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simple in-memory cache for Patient records, keyed by patient ID.
 *
 * Conceptually, this mirrors what a database index does: instead of scanning every
 * row to find a patient, we keep a direct Map (hash table) from ID -> Patient, giving
 * O(1) average-case lookup instead of the O(n) scan a fresh query would need. A database
 * B-tree index gives O(log n) lookup on disk; this in-memory HashMap gives O(1) lookup in
 * RAM — the same underlying idea (avoid scanning everything) applied at a different layer.
 *
 * Cache invalidation strategy: this cache is intentionally "dumb" — any write operation
 * (add/update/delete) clears the whole cache rather than trying to patch individual entries.
 * This trades a little efficiency (next read after a write has to hit the DB again) for
 * correctness and simplicity: there's no risk of a stale or partially-updated entry ever
 * being served, which is the failure mode that matters most for patient data.
 */
public class PatientCache {

    // LinkedHashMap preserves insertion order, which incidentally makes debugging/logging easier.
    private final Map<Integer, Patient> cache = new LinkedHashMap<>();
    private boolean fullyLoaded = false; // tracks whether cache currently represents "all patients"

    public Optional<Patient> get(int patientId) {
        return Optional.ofNullable(cache.get(patientId));
    }

    public void put(Patient patient) {
        cache.put(patient.getPatientId(), patient);
    }

    public void putAll(Iterable<Patient> patients) {
        for (Patient p : patients) {
            cache.put(p.getPatientId(), p);
        }
        fullyLoaded = true;
    }

    public boolean isFullyLoaded() {
        return fullyLoaded;
    }

    public Map<Integer, Patient> getAll() {
        return cache;
    }

    // Invalidation: called after ANY add/update/delete so the next read goes back to the DB.
    public void invalidate() {
        cache.clear();
        fullyLoaded = false;
    }

    public int size() {
        return cache.size();
    }
}
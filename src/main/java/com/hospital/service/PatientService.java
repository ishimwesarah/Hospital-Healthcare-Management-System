package com.hospital.service;

import com.hospital.cache.PatientCache;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;
import com.hospital.util.PatientSorter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Service layer: validates data, coordinates with the DAO, and manages the in-memory cache.
// UI code should call this, not PatientDAO directly.
public class PatientService {

    private final PatientDAO patientDAO = new PatientDAO();
    private final PatientCache cache = new PatientCache();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+][0-9\\s-]{6,19}$");

    // Returns all patients, sorted by name. Uses the cache if it's already fully loaded,
    // otherwise hits the DB once and populates the cache for next time.
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients;

        if (cache.isFullyLoaded()) {
            System.out.println("[Cache] HIT — serving " + cache.size() + " patients from in-memory cache (no DB query).");
            patients = new ArrayList<>(cache.getAll().values());
        } else {
            System.out.println("[Cache] MISS — querying database and populating cache.");
            patients = patientDAO.findAll();
            cache.putAll(patients);
        }

        return PatientSorter.mergeSortByName(patients);
    }

    // Search bypasses the cache — a filtered search on arbitrary text doesn't map cleanly
    // onto a full-table cache, so this always queries the DB directly via the indexed search.
    public List<Patient> searchPatients(String searchTerm) throws SQLException {
        return patientDAO.searchPatients(searchTerm);
    }

    // Single-record lookup: check cache first (O(1) hash map lookup), fall back to DB on a miss.
    public Patient getPatientById(int id) throws SQLException {
        return cache.get(id).orElseGet(() -> {
            try {
                Patient p = patientDAO.findById(id);
                if (p != null) {
                    cache.put(p);
                }
                return p;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Validates, then creates. Throws IllegalArgumentException with a user-facing message on bad input.
    public int addPatient(Patient patient) throws SQLException {
        validate(patient);
        int id = patientDAO.create(patient);
        System.out.println("[Cache] INVALIDATED — new patient added, cache cleared.");
        cache.invalidate();
        return id;
    }

    public boolean updatePatient(Patient patient) throws SQLException {
        validate(patient);
        boolean success = patientDAO.update(patient);
        if (success) {
            System.out.println("[Cache] INVALIDATED — patient updated, cache cleared.");
            cache.invalidate();
        }
        return success;
    }

    public boolean deletePatient(int id) throws SQLException {
        boolean success = patientDAO.delete(id);
        if (success) {
            System.out.println("[Cache] INVALIDATED — patient deleted, cache cleared.");
            cache.invalidate();
        }
        return success;
    }

    private void validate(Patient patient) {
        if (isBlank(patient.getFirstName())) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (isBlank(patient.getLastName())) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (patient.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of birth is required.");
        }
        if (patient.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future.");
        }
        if (isBlank(patient.getGender())) {
            throw new IllegalArgumentException("Gender is required.");
        }
        if (!isBlank(patient.getEmail()) && !EMAIL_PATTERN.matcher(patient.getEmail()).matches()) {
            throw new IllegalArgumentException("Email format is invalid.");
        }
        if (!isBlank(patient.getPhoneNumber()) && !PHONE_PATTERN.matcher(patient.getPhoneNumber()).matches()) {
            throw new IllegalArgumentException("Phone number format is invalid.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
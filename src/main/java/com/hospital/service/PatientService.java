package com.hospital.service;

import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

// Service layer: validates data and coordinates with the DAO.
// UI code should call this, not PatientDAO directly.
public class PatientService {

    private final PatientDAO patientDAO = new PatientDAO();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+][0-9\\s-]{6,19}$");

    public List<Patient> getAllPatients() throws SQLException {
        return patientDAO.findAll();
    }

    public List<Patient> searchPatients(String searchTerm) throws SQLException {
        return patientDAO.searchPatients(searchTerm);
    }

    public Patient getPatientById(int id) throws SQLException {
        return patientDAO.findById(id);
    }

    // Validates, then creates. Throws IllegalArgumentException with a user-facing message on bad input.
    public int addPatient(Patient patient) throws SQLException {
        validate(patient);
        return patientDAO.create(patient);
    }

    public boolean updatePatient(Patient patient) throws SQLException {
        validate(patient);
        return patientDAO.update(patient);
    }

    public boolean deletePatient(int id) throws SQLException {
        return patientDAO.delete(id);
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
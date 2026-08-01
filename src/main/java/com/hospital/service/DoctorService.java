package com.hospital.service;

import com.hospital.dao.DepartmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.model.Doctor;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class DoctorService {

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO(); // used to verify department exists

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+][0-9\\s-]{6,19}$");

    public List<Doctor> getAllDoctors() throws SQLException {
        return doctorDAO.findAll();
    }

    public Doctor getDoctorById(int id) throws SQLException {
        return doctorDAO.findById(id);
    }

    public List<Doctor> getDoctorsByDepartment(int departmentId) throws SQLException {
        return doctorDAO.findByDepartmentId(departmentId);
    }

    public int addDoctor(Doctor doctor) throws SQLException {
        validate(doctor);
        return doctorDAO.create(doctor);
    }

    public boolean updateDoctor(Doctor doctor) throws SQLException {
        validate(doctor);
        return doctorDAO.update(doctor);
    }

    public boolean deleteDoctor(int id) throws SQLException {
        return doctorDAO.delete(id);
    }

    private void validate(Doctor doctor) throws SQLException {
        if (isBlank(doctor.getFirstName())) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (isBlank(doctor.getLastName())) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (!isBlank(doctor.getEmail()) && !EMAIL_PATTERN.matcher(doctor.getEmail()).matches()) {
            throw new IllegalArgumentException("Email format is invalid.");
        }
        if (!isBlank(doctor.getPhoneNumber()) && !PHONE_PATTERN.matcher(doctor.getPhoneNumber()).matches()) {
            throw new IllegalArgumentException("Phone number format is invalid.");
        }
        // App-level referential integrity check, in addition to the DB's own foreign key constraint.
        // Doing it here lets us give a clear message instead of a raw SQL constraint violation.
        if (departmentDAO.findById(doctor.getDepartmentId()) == null) {
            throw new IllegalArgumentException("Selected department does not exist.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
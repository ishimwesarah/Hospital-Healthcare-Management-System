package com.hospital.dao;

import com.hospital.db.DatabaseConnection;
import com.hospital.model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    // CREATE — insert a new patient, return the auto-generated id
    public int create(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (first_name, last_name, date_of_birth, gender, phone_number, email, address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            stmt.setDate(3, java.sql.Date.valueOf(patient.getDateOfBirth()));
            stmt.setString(4, patient.getGender());
            stmt.setString(5, patient.getPhoneNumber());
            stmt.setString(6, patient.getEmail());
            stmt.setString(7, patient.getAddress());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating patient failed, no ID obtained.");
                }
            }
        }
    }

    // READ — find one patient by id
    public Patient findById(int patientId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPatient(rs);
                } else {
                    return null; // no patient found with that id
                }
            }
        }
    }

    // READ — find all patients
    public List<Patient> findAll() throws SQLException {
        String sql = "SELECT * FROM patients ORDER BY last_name, first_name";
        List<Patient> patients = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                patients.add(mapRowToPatient(rs));
            }
        }

        return patients;
    }

    // UPDATE — update an existing patient's details
    public boolean update(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, date_of_birth = ?, " +
                "gender = ?, phone_number = ?, email = ?, address = ? WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            stmt.setDate(3, java.sql.Date.valueOf(patient.getDateOfBirth()));
            stmt.setString(4, patient.getGender());
            stmt.setString(5, patient.getPhoneNumber());
            stmt.setString(6, patient.getEmail());
            stmt.setString(7, patient.getAddress());
            stmt.setInt(8, patient.getPatientId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // DELETE — remove a patient by id
    public boolean delete(int patientId) throws SQLException {
        String sql = "DELETE FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Helper: map one ResultSet row to a Patient object
    private Patient mapRowToPatient(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("patient_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getDate("date_of_birth").toLocalDate(),
                rs.getString("gender"),
                rs.getString("phone_number"),
                rs.getString("email"),
                rs.getString("address")
        );
    }
}
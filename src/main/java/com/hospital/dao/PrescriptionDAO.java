package com.hospital.dao;

import com.hospital.db.DatabaseConnection;
import com.hospital.model.Prescription;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    public int create(Prescription prescription) throws SQLException {
        String sql = "INSERT INTO prescriptions (patient_id, doctor_id, date_prescribed, notes) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, prescription.getPatientId());
            stmt.setInt(2, prescription.getDoctorId());
            stmt.setDate(3, java.sql.Date.valueOf(prescription.getDatePrescribed()));
            stmt.setString(4, prescription.getNotes());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating prescription failed, no ID obtained.");
                }
            }
        }
    }

    public Prescription findById(int prescriptionId) throws SQLException {
        String sql = "SELECT * FROM prescriptions WHERE prescription_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, prescriptionId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRowToPrescription(rs) : null;
            }
        }
    }

    public List<Prescription> findAll() throws SQLException {
        String sql = "SELECT * FROM prescriptions";
        List<Prescription> prescriptions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                prescriptions.add(mapRowToPrescription(rs));
            }
        }

        return prescriptions;
    }

    // Joined query — used by the UI so patient/doctor names show instead of raw IDs.
    public List<Prescription> findAllWithNames() throws SQLException {
        String sql = "SELECT pr.*, " +
                "       p.first_name AS patient_first_name, p.last_name AS patient_last_name, " +
                "       d.first_name AS doctor_first_name, d.last_name AS doctor_last_name " +
                "FROM prescriptions pr " +
                "JOIN patients p ON pr.patient_id = p.patient_id " +
                "JOIN doctors d ON pr.doctor_id = d.doctor_id " +
                "ORDER BY pr.date_prescribed DESC";

        List<Prescription> prescriptions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Prescription pr = mapRowToPrescription(rs);
                pr.setPatientName(rs.getString("patient_first_name") + " " + rs.getString("patient_last_name"));
                pr.setDoctorName(rs.getString("doctor_first_name") + " " + rs.getString("doctor_last_name"));
                prescriptions.add(pr);
            }
        }

        return prescriptions;
    }

    public boolean update(Prescription prescription) throws SQLException {
        String sql = "UPDATE prescriptions SET patient_id = ?, doctor_id = ?, date_prescribed = ?, notes = ? " +
                "WHERE prescription_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, prescription.getPatientId());
            stmt.setInt(2, prescription.getDoctorId());
            stmt.setDate(3, java.sql.Date.valueOf(prescription.getDatePrescribed()));
            stmt.setString(4, prescription.getNotes());
            stmt.setInt(5, prescription.getPrescriptionId());

            return stmt.executeUpdate() > 0;
        }
    }

    // Deleting a prescription cascades to prescription_items automatically (ON DELETE CASCADE in schema).
    public boolean delete(int prescriptionId) throws SQLException {
        String sql = "DELETE FROM prescriptions WHERE prescription_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, prescriptionId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Prescription mapRowToPrescription(ResultSet rs) throws SQLException {
        return new Prescription(
                rs.getInt("prescription_id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getDate("date_prescribed").toLocalDate(),
                rs.getString("notes")
        );
    }
}
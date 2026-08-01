package com.hospital.dao;

import com.hospital.db.DatabaseConnection;
import com.hospital.model.PatientFeedback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PatientFeedbackDAO {

    public int create(PatientFeedback feedback) throws SQLException {
        String sql = "INSERT INTO patient_feedback (patient_id, doctor_id, rating, comments, feedback_date) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, feedback.getPatientId());
            setNullableInt(stmt, 2, feedback.getDoctorId());
            stmt.setInt(3, feedback.getRating());
            stmt.setString(4, feedback.getComments());
            stmt.setDate(5, java.sql.Date.valueOf(feedback.getFeedbackDate()));

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating patient feedback failed, no ID obtained.");
                }
            }
        }
    }

    public PatientFeedback findById(int feedbackId) throws SQLException {
        String sql = "SELECT * FROM patient_feedback WHERE feedback_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, feedbackId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRowToFeedback(rs) : null;
            }
        }
    }

    public List<PatientFeedback> findAll() throws SQLException {
        String sql = "SELECT * FROM patient_feedback";
        List<PatientFeedback> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(mapRowToFeedback(rs));
            }
        }

        return results;
    }

    // Joined query so the UI can show patient/doctor names instead of raw IDs.
    // LEFT JOIN on doctors since doctor_id is nullable — feedback with no doctor should still show up.
    public List<PatientFeedback> findAllWithNames() throws SQLException {
        String sql = "SELECT f.*, " +
                "       p.first_name AS patient_first_name, p.last_name AS patient_last_name, " +
                "       d.first_name AS doctor_first_name, d.last_name AS doctor_last_name " +
                "FROM patient_feedback f " +
                "JOIN patients p ON f.patient_id = p.patient_id " +
                "LEFT JOIN doctors d ON f.doctor_id = d.doctor_id " +
                "ORDER BY f.feedback_date DESC";

        List<PatientFeedback> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PatientFeedback fb = mapRowToFeedback(rs);
                fb.setPatientName(rs.getString("patient_first_name") + " " + rs.getString("patient_last_name"));

                String doctorFirst = rs.getString("doctor_first_name");
                fb.setDoctorName(doctorFirst != null
                        ? "Dr. " + doctorFirst + " " + rs.getString("doctor_last_name")
                        : "(General feedback)");

                results.add(fb);
            }
        }

        return results;
    }

    public boolean update(PatientFeedback feedback) throws SQLException {
        String sql = "UPDATE patient_feedback SET patient_id = ?, doctor_id = ?, rating = ?, comments = ?, feedback_date = ? " +
                "WHERE feedback_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, feedback.getPatientId());
            setNullableInt(stmt, 2, feedback.getDoctorId());
            stmt.setInt(3, feedback.getRating());
            stmt.setString(4, feedback.getComments());
            stmt.setDate(5, java.sql.Date.valueOf(feedback.getFeedbackDate()));
            stmt.setInt(6, feedback.getFeedbackId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int feedbackId) throws SQLException {
        String sql = "DELETE FROM patient_feedback WHERE feedback_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, feedbackId);
            return stmt.executeUpdate() > 0;
        }
    }

    private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(index, value);
        } else {
            stmt.setNull(index, Types.INTEGER);
        }
    }

    private PatientFeedback mapRowToFeedback(ResultSet rs) throws SQLException {
        int doctorId = rs.getInt("doctor_id");
        Integer doctorIdObj = rs.wasNull() ? null : doctorId;

        return new PatientFeedback(
                rs.getInt("feedback_id"),
                rs.getInt("patient_id"),
                doctorIdObj,
                rs.getInt("rating"),
                rs.getString("comments"),
                rs.getDate("feedback_date").toLocalDate()
        );
    }
}
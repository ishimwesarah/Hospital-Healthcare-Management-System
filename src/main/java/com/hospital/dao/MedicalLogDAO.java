package com.hospital.dao;

import com.hospital.db.DatabaseConnection;
import com.hospital.model.MedicalLog;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedicalLogDAO {

    public int create(MedicalLog log) throws SQLException {
        String sql = "INSERT INTO medical_logs (patient_id, log_data) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, log.getPatientId());
            stmt.setObject(2, toPGobject(log.getLogDataJson()));

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating medical log failed, no ID obtained.");
                }
            }
        }
    }

    public List<MedicalLog> findByPatientId(int patientId) throws SQLException {
        String sql = "SELECT * FROM medical_logs WHERE patient_id = ? ORDER BY log_date DESC";
        List<MedicalLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToLog(rs));
                }
            }
        }

        return logs;
    }

    public List<MedicalLog> findAll() throws SQLException {
        String sql = "SELECT * FROM medical_logs ORDER BY log_date DESC";
        List<MedicalLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                logs.add(mapRowToLog(rs));
            }
        }

        return logs;
    }

    // Demonstrates JSONB containment querying: finds logs where log_data CONTAINS the given
    // key/value pair, regardless of what other fields that particular log entry has.
    // Example call: findByJsonContains("{\"severity\": \"high\"}")
    // This is the core advantage being demonstrated — querying INSIDE a flexible structure
    // without needing a fixed column for every possible field.
    public List<MedicalLog> findByJsonContains(String jsonFragment) throws SQLException {
        String sql = "SELECT * FROM medical_logs WHERE log_data @> ?::jsonb ORDER BY log_date DESC";
        List<MedicalLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, jsonFragment);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToLog(rs));
                }
            }
        }

        return logs;
    }

    public boolean delete(int logId) throws SQLException {
        String sql = "DELETE FROM medical_logs WHERE log_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, logId);
            return stmt.executeUpdate() > 0;
        }
    }

    private PGobject toPGobject(String json) throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("jsonb");
        pgObject.setValue(json);
        return pgObject;
    }

    private MedicalLog mapRowToLog(ResultSet rs) throws SQLException {
        return new MedicalLog(
                rs.getInt("log_id"),
                rs.getInt("patient_id"),
                rs.getTimestamp("log_date").toLocalDateTime(),
                rs.getString("log_data")
        );
    }
}
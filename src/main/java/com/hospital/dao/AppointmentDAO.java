package com.hospital.dao;

import com.hospital.db.DatabaseConnection;
import com.hospital.model.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public int create(Appointment appt) throws SQLException {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, reason, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, appt.getPatientId());
            stmt.setInt(2, appt.getDoctorId());
            stmt.setDate(3, java.sql.Date.valueOf(appt.getAppointmentDate()));
            stmt.setTime(4, java.sql.Time.valueOf(appt.getAppointmentTime()));
            stmt.setString(5, appt.getReason());
            stmt.setString(6, appt.getStatus());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating appointment failed, no ID obtained.");
                }
            }
        }
    }

    public Appointment findById(int appointmentId) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRowToAppointment(rs) : null;
            }
        }
    }

    // Plain list, no joins — used internally / for tests
    public List<Appointment> findAll() throws SQLException {
        String sql = "SELECT * FROM appointments ORDER BY appointment_date, appointment_time";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                appointments.add(mapRowToAppointment(rs));
            }
        }

        return appointments;
    }

    // Joined query — this is what the UI uses, since showing raw patient_id/doctor_id isn't useful to a receptionist.
    public List<Appointment> findAllWithNames() throws SQLException {
        String sql = "SELECT a.*, " +
                "       p.first_name AS patient_first_name, p.last_name AS patient_last_name, " +
                "       d.first_name AS doctor_first_name, d.last_name AS doctor_last_name " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                "ORDER BY a.appointment_date, a.appointment_time";

        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Appointment appt = mapRowToAppointment(rs);
                appt.setPatientName(rs.getString("patient_first_name") + " " + rs.getString("patient_last_name"));
                appt.setDoctorName(rs.getString("doctor_first_name") + " " + rs.getString("doctor_last_name"));
                appointments.add(appt);
            }
        }

        return appointments;
    }

    public boolean update(Appointment appt) throws SQLException {
        String sql = "UPDATE appointments SET patient_id = ?, doctor_id = ?, appointment_date = ?, " +
                "appointment_time = ?, reason = ?, status = ? WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appt.getPatientId());
            stmt.setInt(2, appt.getDoctorId());
            stmt.setDate(3, java.sql.Date.valueOf(appt.getAppointmentDate()));
            stmt.setTime(4, java.sql.Time.valueOf(appt.getAppointmentTime()));
            stmt.setString(5, appt.getReason());
            stmt.setString(6, appt.getStatus());
            stmt.setInt(7, appt.getAppointmentId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int appointmentId) throws SQLException {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Appointment mapRowToAppointment(ResultSet rs) throws SQLException {
        return new Appointment(
                rs.getInt("appointment_id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getDate("appointment_date").toLocalDate(),
                rs.getTime("appointment_time").toLocalTime(),
                rs.getString("reason"),
                rs.getString("status")
        );
    }
}
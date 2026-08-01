package com.hospital.dao;

import com.hospital.db.DatabaseConnection;
import com.hospital.model.Doctor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    public int create(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO doctors (first_name, last_name, specialization, phone_number, email, department_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, doctor.getFirstName());
            stmt.setString(2, doctor.getLastName());
            stmt.setString(3, doctor.getSpecialization());
            stmt.setString(4, doctor.getPhoneNumber());
            stmt.setString(5, doctor.getEmail());
            stmt.setInt(6, doctor.getDepartmentId());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating doctor failed, no ID obtained.");
                }
            }
        }
    }

    public Doctor findById(int doctorId) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRowToDoctor(rs) : null;
            }
        }
    }

    public List<Doctor> findAll() throws SQLException {
        String sql = "SELECT * FROM doctors ORDER BY last_name, first_name";
        List<Doctor> doctors = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                doctors.add(mapRowToDoctor(rs));
            }
        }

        return doctors;
    }

    // Bonus: find doctors by department — useful later for appointments/scheduling screens
    public List<Doctor> findByDepartmentId(int departmentId) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE department_id = ? ORDER BY last_name, first_name";
        List<Doctor> doctors = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    doctors.add(mapRowToDoctor(rs));
                }
            }
        }

        return doctors;
    }

    public boolean update(Doctor doctor) throws SQLException {
        String sql = "UPDATE doctors SET first_name = ?, last_name = ?, specialization = ?, " +
                "phone_number = ?, email = ?, department_id = ? WHERE doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctor.getFirstName());
            stmt.setString(2, doctor.getLastName());
            stmt.setString(3, doctor.getSpecialization());
            stmt.setString(4, doctor.getPhoneNumber());
            stmt.setString(5, doctor.getEmail());
            stmt.setInt(6, doctor.getDepartmentId());
            stmt.setInt(7, doctor.getDoctorId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int doctorId) throws SQLException {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Doctor mapRowToDoctor(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("doctor_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("specialization"),
                rs.getString("phone_number"),
                rs.getString("email"),
                rs.getInt("department_id")
        );
    }
}
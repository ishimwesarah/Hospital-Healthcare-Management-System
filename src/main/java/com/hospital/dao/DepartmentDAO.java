package com.hospital.dao;

import com.hospital.db.DatabaseConnection;
import com.hospital.model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public int create(Department department) throws SQLException {
        String sql = "INSERT INTO departments (name, location, phone_number) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, department.getName());
            stmt.setString(2, department.getLocation());
            stmt.setString(3, department.getPhoneNumber());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating department failed, no ID obtained.");
                }
            }
        }
    }

    public Department findById(int departmentId) throws SQLException {
        String sql = "SELECT * FROM departments WHERE department_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRowToDepartment(rs) : null;
            }
        }
    }

    public List<Department> findAll() throws SQLException {
        String sql = "SELECT * FROM departments ORDER BY name";
        List<Department> departments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                departments.add(mapRowToDepartment(rs));
            }
        }

        return departments;
    }

    public boolean update(Department department) throws SQLException {
        String sql = "UPDATE departments SET name = ?, location = ?, phone_number = ? WHERE department_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, department.getName());
            stmt.setString(2, department.getLocation());
            stmt.setString(3, department.getPhoneNumber());
            stmt.setInt(4, department.getDepartmentId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int departmentId) throws SQLException {
        String sql = "DELETE FROM departments WHERE department_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Department mapRowToDepartment(ResultSet rs) throws SQLException {
        return new Department(
                rs.getInt("department_id"),
                rs.getString("name"),
                rs.getString("location"),
                rs.getString("phone_number")
        );
    }
}
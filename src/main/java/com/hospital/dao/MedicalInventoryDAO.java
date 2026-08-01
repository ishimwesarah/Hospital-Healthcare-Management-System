package com.hospital.dao;

import com.hospital.db.DatabaseConnection;
import com.hospital.model.MedicalInventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedicalInventoryDAO {

    public int create(MedicalInventory item) throws SQLException {
        String sql = "INSERT INTO medical_inventory (item_name, quantity, unit, reorder_level, last_restocked) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getItemName());
            stmt.setInt(2, item.getQuantity());
            stmt.setString(3, item.getUnit());
            stmt.setInt(4, item.getReorderLevel());
            stmt.setDate(5, item.getLastRestocked() != null ? java.sql.Date.valueOf(item.getLastRestocked()) : null);

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating inventory item failed, no ID obtained.");
                }
            }
        }
    }

    public MedicalInventory findById(int inventoryId) throws SQLException {
        String sql = "SELECT * FROM medical_inventory WHERE inventory_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, inventoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRowToInventory(rs) : null;
            }
        }
    }

    public List<MedicalInventory> findAll() throws SQLException {
        String sql = "SELECT * FROM medical_inventory ORDER BY item_name";
        List<MedicalInventory> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(mapRowToInventory(rs));
            }
        }

        return results;
    }

    public boolean update(MedicalInventory item) throws SQLException {
        String sql = "UPDATE medical_inventory SET item_name = ?, quantity = ?, unit = ?, " +
                "reorder_level = ?, last_restocked = ? WHERE inventory_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getItemName());
            stmt.setInt(2, item.getQuantity());
            stmt.setString(3, item.getUnit());
            stmt.setInt(4, item.getReorderLevel());
            stmt.setDate(5, item.getLastRestocked() != null ? java.sql.Date.valueOf(item.getLastRestocked()) : null);
            stmt.setInt(6, item.getInventoryId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int inventoryId) throws SQLException {
        String sql = "DELETE FROM medical_inventory WHERE inventory_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, inventoryId);
            return stmt.executeUpdate() > 0;
        }
    }

    private MedicalInventory mapRowToInventory(ResultSet rs) throws SQLException {
        java.sql.Date restocked = rs.getDate("last_restocked");
        return new MedicalInventory(
                rs.getInt("inventory_id"),
                rs.getString("item_name"),
                rs.getInt("quantity"),
                rs.getString("unit"),
                rs.getInt("reorder_level"),
                restocked != null ? restocked.toLocalDate() : null
        );
    }
}
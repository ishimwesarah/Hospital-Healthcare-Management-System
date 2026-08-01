package com.hospital.dao;

import com.hospital.db.DatabaseConnection;
import com.hospital.model.PrescriptionItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionItemDAO {

    public int create(PrescriptionItem item) throws SQLException {
        String sql = "INSERT INTO prescription_items (prescription_id, medication_name, dosage, frequency, duration, quantity) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, item.getPrescriptionId());
            stmt.setString(2, item.getMedicationName());
            stmt.setString(3, item.getDosage());
            stmt.setString(4, item.getFrequency());
            stmt.setString(5, item.getDuration());
            stmt.setInt(6, item.getQuantity());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating prescription item failed, no ID obtained.");
                }
            }
        }
    }

    public List<PrescriptionItem> findByPrescriptionId(int prescriptionId) throws SQLException {
        String sql = "SELECT * FROM prescription_items WHERE prescription_id = ? ORDER BY prescription_item_id";
        List<PrescriptionItem> items = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, prescriptionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRowToItem(rs));
                }
            }
        }

        return items;
    }

    // Deletes ALL items belonging to a prescription — used when replacing the item set on update.
    public void deleteByPrescriptionId(int prescriptionId) throws SQLException {
        String sql = "DELETE FROM prescription_items WHERE prescription_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, prescriptionId);
            stmt.executeUpdate();
        }
    }

    private PrescriptionItem mapRowToItem(ResultSet rs) throws SQLException {
        return new PrescriptionItem(
                rs.getInt("prescription_item_id"),
                rs.getInt("prescription_id"),
                rs.getString("medication_name"),
                rs.getString("dosage"),
                rs.getString("frequency"),
                rs.getString("duration"),
                rs.getInt("quantity")
        );
    }
}
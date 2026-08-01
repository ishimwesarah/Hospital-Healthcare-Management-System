package com.hospital.service;

import com.hospital.dao.MedicalInventoryDAO;
import com.hospital.model.MedicalInventory;

import java.sql.SQLException;
import java.util.List;

public class MedicalInventoryService {

    private final MedicalInventoryDAO inventoryDAO = new MedicalInventoryDAO();

    public List<MedicalInventory> getAllInventory() throws SQLException {
        return inventoryDAO.findAll();
    }

    public MedicalInventory getInventoryById(int id) throws SQLException {
        return inventoryDAO.findById(id);
    }

    public int addInventoryItem(MedicalInventory item) throws SQLException {
        validate(item);
        return inventoryDAO.create(item);
    }

    public boolean updateInventoryItem(MedicalInventory item) throws SQLException {
        validate(item);
        return inventoryDAO.update(item);
    }

    public boolean deleteInventoryItem(int id) throws SQLException {
        return inventoryDAO.delete(id);
    }

    // Convenience for a future dashboard/report: items that need restocking.
    public List<MedicalInventory> getLowStockItems() throws SQLException {
        return inventoryDAO.findAll().stream()
                .filter(MedicalInventory::isBelowReorderLevel)
                .toList();
    }

    private void validate(MedicalInventory item) {
        if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name is required.");
        }
        if (item.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        if (item.getReorderLevel() < 0) {
            throw new IllegalArgumentException("Reorder level cannot be negative.");
        }
    }
}
package com.hospital.model;

import java.time.LocalDate;

public class MedicalInventory {

    private int inventoryId;
    private String itemName;
    private int quantity;
    private String unit;
    private int reorderLevel;
    private LocalDate lastRestocked;

    public MedicalInventory() {
    }

    // Use for a NEW inventory item (id not yet known)
    public MedicalInventory(String itemName, int quantity, String unit, int reorderLevel, LocalDate lastRestocked) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.reorderLevel = reorderLevel;
        this.lastRestocked = lastRestocked;
    }

    // Use when reading FROM the database (id is known)
    public MedicalInventory(int inventoryId, String itemName, int quantity, String unit,
                            int reorderLevel, LocalDate lastRestocked) {
        this.inventoryId = inventoryId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.reorderLevel = reorderLevel;
        this.lastRestocked = lastRestocked;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public LocalDate getLastRestocked() {
        return lastRestocked;
    }

    public void setLastRestocked(LocalDate lastRestocked) {
        this.lastRestocked = lastRestocked;
    }

    // Convenience method — useful for the UI to visually flag low-stock items
    public boolean isBelowReorderLevel() {
        return quantity < reorderLevel;
    }

    @Override
    public String toString() {
        return "MedicalInventory{" +
                "inventoryId=" + inventoryId +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", reorderLevel=" + reorderLevel +
                ", lastRestocked=" + lastRestocked +
                '}';
    }
}
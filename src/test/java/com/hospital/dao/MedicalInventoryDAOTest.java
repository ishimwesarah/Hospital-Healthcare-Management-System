package com.hospital.dao;

import com.hospital.model.MedicalInventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MedicalInventoryDAOTest {

    private final MedicalInventoryDAO dao = new MedicalInventoryDAO();
    private Integer createdInventoryId;

    @Test
    void create_shouldInsertItemAndReturnGeneratedId() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        MedicalInventory item = new MedicalInventory("Test Bandages " + suffix, 100, "boxes", 20, LocalDate.now());

        int id = dao.create(item);
        createdInventoryId = id;

        assertTrue(id > 0);
    }

    @Test
    void findById_shouldReturnCorrectItem() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        MedicalInventory item = new MedicalInventory("Test Syringes " + suffix, 50, "units", 10, LocalDate.now());
        int id = dao.create(item);
        createdInventoryId = id;

        MedicalInventory fetched = dao.findById(id);

        assertNotNull(fetched);
        assertEquals(50, fetched.getQuantity());
        assertEquals("units", fetched.getUnit());
    }

    @Test
    void update_shouldModifyQuantity() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        MedicalInventory item = new MedicalInventory("Test Gloves " + suffix, 200, "boxes", 30, LocalDate.now());
        int id = dao.create(item);
        createdInventoryId = id;

        MedicalInventory toUpdate = dao.findById(id);
        toUpdate.setQuantity(15); // below reorder level of 30

        boolean updated = dao.update(toUpdate);
        MedicalInventory afterUpdate = dao.findById(id);

        assertTrue(updated);
        assertEquals(15, afterUpdate.getQuantity());
        assertTrue(afterUpdate.isBelowReorderLevel());
    }

    @Test
    void delete_shouldRemoveItem() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        MedicalInventory item = new MedicalInventory("Test Masks " + suffix, 500, "boxes", 50, LocalDate.now());
        int id = dao.create(item);

        boolean deleted = dao.delete(id);
        MedicalInventory afterDelete = dao.findById(id);

        assertTrue(deleted);
        assertNull(afterDelete);

        createdInventoryId = null;
    }

    @Test
    void findAll_shouldReturnListContainingCreatedItem() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        MedicalInventory item = new MedicalInventory("Test Thermometers " + suffix, 30, "units", 5, LocalDate.now());
        int id = dao.create(item);
        createdInventoryId = id;

        List<MedicalInventory> all = dao.findAll();

        assertTrue(all.stream().anyMatch(i -> i.getInventoryId() == id));
    }

    @AfterEach
    void cleanUp() throws Exception {
        if (createdInventoryId != null) {
            dao.delete(createdInventoryId);
            createdInventoryId = null;
        }
    }
}
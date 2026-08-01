package com.hospital.dao;

import com.hospital.model.Department;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentDAOTest {

    private final DepartmentDAO dao = new DepartmentDAO();
    private Integer createdDepartmentId;

    @Test
    void create_shouldInsertDepartmentAndReturnGeneratedId() throws Exception {
        Department dept = new Department("Cardiology Test", "Block A", "0788100100");

        int id = dao.create(dept);
        createdDepartmentId = id;

        assertTrue(id > 0);
    }

    @Test
    void findById_shouldReturnCorrectDepartment() throws Exception {
        Department dept = new Department("Neurology Test", "Block B", "0788200200");
        int id = dao.create(dept);
        createdDepartmentId = id;

        Department fetched = dao.findById(id);

        assertNotNull(fetched);
        assertEquals("Neurology Test", fetched.getName());
        assertEquals("Block B", fetched.getLocation());
    }

    @Test
    void update_shouldModifyExistingDepartment() throws Exception {
        Department dept = new Department("Pediatrics Test", "Block C", "0788300300");
        int id = dao.create(dept);
        createdDepartmentId = id;

        Department toUpdate = dao.findById(id);
        toUpdate.setLocation("Block D");

        boolean updated = dao.update(toUpdate);
        Department afterUpdate = dao.findById(id);

        assertTrue(updated);
        assertEquals("Block D", afterUpdate.getLocation());
    }

    @Test
    void delete_shouldRemoveDepartment() throws Exception {
        Department dept = new Department("Radiology Test", "Block E", "0788400400");
        int id = dao.create(dept);

        boolean deleted = dao.delete(id);
        Department afterDelete = dao.findById(id);

        assertTrue(deleted);
        assertNull(afterDelete);

        createdDepartmentId = null;
    }

    @Test
    void findAll_shouldReturnListContainingCreatedDepartment() throws Exception {
        Department dept = new Department("Oncology Test", "Block F", "0788500500");
        int id = dao.create(dept);
        createdDepartmentId = id;

        List<Department> all = dao.findAll();

        assertTrue(all.stream().anyMatch(d -> d.getDepartmentId() == id));
    }

    @AfterEach
    void cleanUp() throws Exception {
        if (createdDepartmentId != null) {
            dao.delete(createdDepartmentId);
            createdDepartmentId = null;
        }
    }
}
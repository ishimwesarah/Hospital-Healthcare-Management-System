package com.hospital.dao;

import com.hospital.model.Department;
import com.hospital.model.Doctor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DoctorDAOTest {

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    private int testDepartmentId;
    private Integer createdDoctorId;

    // Every doctor needs a valid department_id (foreign key), so create one before each test
    @BeforeEach
    void setUpDepartment() throws Exception {
        Department dept = new Department("Test Department", "Test Block", "0788000111");
        testDepartmentId = departmentDAO.create(dept);
    }

    @Test
    void create_shouldInsertDoctorAndReturnGeneratedId() throws Exception {
        Doctor doctor = new Doctor("Alice", "Mugabo", "Cardiology",
                "0788123123", "alice.mugabo@example.com", testDepartmentId);

        int id = doctorDAO.create(doctor);
        createdDoctorId = id;

        assertTrue(id > 0);
    }

    @Test
    void findById_shouldReturnCorrectDoctor() throws Exception {
        Doctor doctor = new Doctor("Bob", "Nshuti", "Neurology",
                "0788456456", "bob.nshuti@example.com", testDepartmentId);
        int id = doctorDAO.create(doctor);
        createdDoctorId = id;

        Doctor fetched = doctorDAO.findById(id);

        assertNotNull(fetched);
        assertEquals("Bob", fetched.getFirstName());
        assertEquals(testDepartmentId, fetched.getDepartmentId());
    }

    @Test
    void update_shouldModifyExistingDoctor() throws Exception {
        Doctor doctor = new Doctor("Clara", "Uwimana", "Pediatrics",
                "0788789789", "clara.uwimana@example.com", testDepartmentId);
        int id = doctorDAO.create(doctor);
        createdDoctorId = id;

        Doctor toUpdate = doctorDAO.findById(id);
        toUpdate.setSpecialization("General Surgery");

        boolean updated = doctorDAO.update(toUpdate);
        Doctor afterUpdate = doctorDAO.findById(id);

        assertTrue(updated);
        assertEquals("General Surgery", afterUpdate.getSpecialization());
    }

    @Test
    void delete_shouldRemoveDoctor() throws Exception {
        Doctor doctor = new Doctor("David", "Habimana", "Radiology",
                "0788321321", "david.habimana@example.com", testDepartmentId);
        int id = doctorDAO.create(doctor);

        boolean deleted = doctorDAO.delete(id);
        Doctor afterDelete = doctorDAO.findById(id);

        assertTrue(deleted);
        assertNull(afterDelete);

        createdDoctorId = null;
    }

    @Test
    void findByDepartmentId_shouldReturnOnlyDoctorsInThatDepartment() throws Exception {
        Doctor doctor = new Doctor("Eva", "Keza", "Oncology",
                "0788654654", "eva.keza@example.com", testDepartmentId);
        int id = doctorDAO.create(doctor);
        createdDoctorId = id;

        List<Doctor> doctorsInDept = doctorDAO.findByDepartmentId(testDepartmentId);

        assertTrue(doctorsInDept.stream().anyMatch(d -> d.getDoctorId() == id));
    }

    @AfterEach
    void cleanUp() throws Exception {
        if (createdDoctorId != null) {
            doctorDAO.delete(createdDoctorId);
            createdDoctorId = null;
        }
        departmentDAO.delete(testDepartmentId);
    }
}
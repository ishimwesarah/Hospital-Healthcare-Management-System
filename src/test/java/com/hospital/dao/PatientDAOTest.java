package com.hospital.dao;

import com.hospital.model.Patient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientDAOTest {

    private final PatientDAO dao = new PatientDAO();
    private Integer createdPatientId; // tracked so we can clean up after each test

    @Test
    void create_shouldInsertPatientAndReturnGeneratedId() throws Exception {
        Patient patient = new Patient(
                "Jane", "Doe", LocalDate.of(1990, 5, 12),
                "Female", "0788123456", "jane.doe@example.com", "Kigali, Rwanda"
        );

        int id = dao.create(patient);
        createdPatientId = id;

        assertTrue(id > 0, "Generated patient id should be positive");
    }

    @Test
    void findById_shouldReturnCorrectPatient() throws Exception {
        Patient patient = new Patient(
                "John", "Smith", LocalDate.of(1985, 3, 20),
                "Male", "0788111222", "john.smith@example.com", "Musanze, Rwanda"
        );
        int id = dao.create(patient);
        createdPatientId = id;

        Patient fetched = dao.findById(id);

        assertNotNull(fetched);
        assertEquals("John", fetched.getFirstName());
        assertEquals("Smith", fetched.getLastName());
        assertEquals("john.smith@example.com", fetched.getEmail());
    }

    @Test
    void update_shouldModifyExistingPatient() throws Exception {
        Patient patient = new Patient(
                "Alice", "Uwase", LocalDate.of(1995, 7, 1),
                "Female", "0788333444", "alice.uwase@example.com", "Huye, Rwanda"
        );
        int id = dao.create(patient);
        createdPatientId = id;

        Patient toUpdate = dao.findById(id);
        toUpdate.setPhoneNumber("0788999999");

        boolean updated = dao.update(toUpdate);
        Patient afterUpdate = dao.findById(id);

        assertTrue(updated);
        assertEquals("0788999999", afterUpdate.getPhoneNumber());
    }

    @Test
    void delete_shouldRemovePatient() throws Exception {
        Patient patient = new Patient(
                "Bob", "Mugisha", LocalDate.of(1980, 11, 30),
                "Male", "0788555666", "bob.mugisha@example.com", "Rubavu, Rwanda"
        );
        int id = dao.create(patient);

        boolean deleted = dao.delete(id);
        Patient afterDelete = dao.findById(id);

        assertTrue(deleted);
        assertNull(afterDelete);

        createdPatientId = null; // already deleted, nothing to clean up
    }

    @Test
    void findAll_shouldReturnListContainingCreatedPatient() throws Exception {
        Patient patient = new Patient(
                "Grace", "Ingabire", LocalDate.of(2000, 1, 15),
                "Female", "0788777888", "grace.ingabire@example.com", "Kigali, Rwanda"
        );
        int id = dao.create(patient);
        createdPatientId = id;

        List<Patient> allPatients = dao.findAll();

        assertTrue(allPatients.stream().anyMatch(p -> p.getPatientId() == id));
    }

    // Clean up whatever this test created, so tests don't pollute the database for each other
    @AfterEach
    void cleanUp() throws Exception {
        if (createdPatientId != null) {
            dao.delete(createdPatientId);
            createdPatientId = null;
        }
    }
}
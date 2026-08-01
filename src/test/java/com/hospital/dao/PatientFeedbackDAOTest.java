package com.hospital.dao;

import com.hospital.model.Department;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.PatientFeedback;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientFeedbackDAOTest {

    private final PatientFeedbackDAO feedbackDAO = new PatientFeedbackDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    private int testDepartmentId;
    private int testDoctorId;
    private int testPatientId;
    private Integer createdFeedbackId;

    @BeforeEach
    void setUpDependencies() throws Exception {
        String suffix = String.valueOf(System.nanoTime());

        testDepartmentId = departmentDAO.create(new Department("Test Dept " + suffix, "Block Z", "0788" + suffix.substring(0, 6)));
        testDoctorId = doctorDAO.create(new Doctor("Test", "Doctor", "General",
                "0789" + suffix.substring(0, 6), "test.doctor." + suffix + "@example.com", testDepartmentId));
        testPatientId = patientDAO.create(new Patient("Test", "Patient",
                LocalDate.of(1999, 1, 1), "Other", "0787" + suffix.substring(0, 6),
                "test.patient." + suffix + "@example.com", "Kigali"));
    }

    @Test
    void create_shouldInsertFeedbackWithDoctor() throws Exception {
        PatientFeedback fb = new PatientFeedback(testPatientId, testDoctorId, 5, "Great service", LocalDate.now());

        int id = feedbackDAO.create(fb);
        createdFeedbackId = id;

        assertTrue(id > 0);
    }

    @Test
    void create_shouldInsertFeedbackWithoutDoctor_generalFeedback() throws Exception {
        PatientFeedback fb = new PatientFeedback(testPatientId, null, 4, "Clean facility", LocalDate.now());

        int id = feedbackDAO.create(fb);
        createdFeedbackId = id;

        PatientFeedback fetched = feedbackDAO.findById(id);
        assertNotNull(fetched);
        assertNull(fetched.getDoctorId());
    }

    @Test
    void findAllWithNames_shouldShowGeneralLabel_whenNoDoctorAttached() throws Exception {
        PatientFeedback fb = new PatientFeedback(testPatientId, null, 3, "Okay experience", LocalDate.now());
        int id = feedbackDAO.create(fb);
        createdFeedbackId = id;

        List<PatientFeedback> all = feedbackDAO.findAllWithNames();
        PatientFeedback found = all.stream().filter(f -> f.getFeedbackId() == id).findFirst().orElse(null);

        assertNotNull(found);
        assertEquals("(General feedback)", found.getDoctorName());
        assertEquals("Test Patient", found.getPatientName());
    }

    @Test
    void update_shouldModifyRatingAndComments() throws Exception {
        PatientFeedback fb = new PatientFeedback(testPatientId, testDoctorId, 2, "Initial comment", LocalDate.now());
        int id = feedbackDAO.create(fb);
        createdFeedbackId = id;

        PatientFeedback toUpdate = feedbackDAO.findById(id);
        toUpdate.setRating(5);
        toUpdate.setComments("Updated comment");

        boolean updated = feedbackDAO.update(toUpdate);
        PatientFeedback afterUpdate = feedbackDAO.findById(id);

        assertTrue(updated);
        assertEquals(5, afterUpdate.getRating());
        assertEquals("Updated comment", afterUpdate.getComments());
    }

    @Test
    void delete_shouldRemoveFeedback() throws Exception {
        PatientFeedback fb = new PatientFeedback(testPatientId, testDoctorId, 1, "To be deleted", LocalDate.now());
        int id = feedbackDAO.create(fb);

        boolean deleted = feedbackDAO.delete(id);
        PatientFeedback afterDelete = feedbackDAO.findById(id);

        assertTrue(deleted);
        assertNull(afterDelete);

        createdFeedbackId = null;
    }

    @AfterEach
    void cleanUp() throws Exception {
        if (createdFeedbackId != null) {
            feedbackDAO.delete(createdFeedbackId);
        }
        patientDAO.delete(testPatientId);
        doctorDAO.delete(testDoctorId);
        departmentDAO.delete(testDepartmentId);
    }
}
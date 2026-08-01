package com.hospital.dao;

import com.hospital.model.Department;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrescriptionDAOTest {

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final PrescriptionItemDAO itemDAO = new PrescriptionItemDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    private int testDepartmentId;
    private int testDoctorId;
    private int testPatientId;
    private Integer createdPrescriptionId;

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
    void create_shouldInsertPrescriptionAndReturnGeneratedId() throws Exception {
        Prescription prescription = new Prescription(testPatientId, testDoctorId, LocalDate.now(), "Test notes");

        int id = prescriptionDAO.create(prescription);
        createdPrescriptionId = id;

        assertTrue(id > 0);
    }

    @Test
    void itemDAO_createAndFindByPrescriptionId_shouldReturnItemsBelongingToPrescription() throws Exception {
        Prescription prescription = new Prescription(testPatientId, testDoctorId, LocalDate.now(), "Test notes");
        int prescriptionId = prescriptionDAO.create(prescription);
        createdPrescriptionId = prescriptionId;

        PrescriptionItem item1 = new PrescriptionItem("Amoxicillin", "500mg", "Twice daily", "7 days", 14);
        item1.setPrescriptionId(prescriptionId);
        itemDAO.create(item1);

        PrescriptionItem item2 = new PrescriptionItem("Paracetamol", "500mg", "As needed", "5 days", 10);
        item2.setPrescriptionId(prescriptionId);
        itemDAO.create(item2);

        List<PrescriptionItem> items = itemDAO.findByPrescriptionId(prescriptionId);

        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(i -> i.getMedicationName().equals("Amoxicillin")));
        assertTrue(items.stream().anyMatch(i -> i.getMedicationName().equals("Paracetamol")));
    }

    @Test
    void deletePrescription_shouldCascadeDeleteItems() throws Exception {
        Prescription prescription = new Prescription(testPatientId, testDoctorId, LocalDate.now(), "To be deleted");
        int prescriptionId = prescriptionDAO.create(prescription);

        PrescriptionItem item = new PrescriptionItem("Ibuprofen", "200mg", "Once daily", "3 days", 3);
        item.setPrescriptionId(prescriptionId);
        itemDAO.create(item);

        boolean deleted = prescriptionDAO.delete(prescriptionId);
        List<PrescriptionItem> remainingItems = itemDAO.findByPrescriptionId(prescriptionId);

        assertTrue(deleted);
        assertTrue(remainingItems.isEmpty(), "Items should be cascade-deleted along with their parent prescription");

        createdPrescriptionId = null; // already deleted
    }

    @Test
    void findAllWithNames_shouldReturnJoinedPatientAndDoctorNames() throws Exception {
        Prescription prescription = new Prescription(testPatientId, testDoctorId, LocalDate.now(), "Consultation notes");
        int id = prescriptionDAO.create(prescription);
        createdPrescriptionId = id;

        List<Prescription> all = prescriptionDAO.findAllWithNames();
        Prescription found = all.stream().filter(p -> p.getPrescriptionId() == id).findFirst().orElse(null);

        assertNotNull(found);
        assertEquals("Test Patient", found.getPatientName());
        assertEquals("Test Doctor", found.getDoctorName());
    }

    @AfterEach
    void cleanUp() throws Exception {
        if (createdPrescriptionId != null) {
            prescriptionDAO.delete(createdPrescriptionId); // cascades to items automatically
        }
        patientDAO.delete(testPatientId);
        doctorDAO.delete(testDoctorId);
        departmentDAO.delete(testDepartmentId);
    }
}
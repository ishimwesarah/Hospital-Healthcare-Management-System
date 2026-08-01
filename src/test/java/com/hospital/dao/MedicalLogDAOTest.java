package com.hospital.dao;

import com.hospital.model.MedicalLog;
import com.hospital.model.Patient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MedicalLogDAOTest {

    private final MedicalLogDAO logDAO = new MedicalLogDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    private int testPatientId;
    private final List<Integer> createdLogIds = new java.util.ArrayList<>();

    @BeforeEach
    void setUpPatient() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        testPatientId = patientDAO.create(new Patient("Test", "Patient",
                LocalDate.of(1999, 1, 1), "Other", "0787" + suffix.substring(0, 6),
                "test.patient." + suffix + "@example.com", "Kigali"));
    }

    @Test
    void create_shouldAcceptDifferentlyShapedJsonForDifferentLogs() throws Exception {
        // First log: a routine checkup shape
        MedicalLog checkupLog = new MedicalLog(testPatientId,
                "{\"type\": \"checkup\", \"vitals\": {\"bp\": \"120/80\", \"temp\": 36.6}}");
        int id1 = logDAO.create(checkupLog);
        createdLogIds.add(id1);

        // Second log: a completely different shape — this is the point being demonstrated
        MedicalLog symptomLog = new MedicalLog(testPatientId,
                "{\"type\": \"symptom_report\", \"symptoms\": [\"cough\", \"fatigue\"], \"severity\": \"moderate\"}");
        int id2 = logDAO.create(symptomLog);
        createdLogIds.add(id2);

        List<MedicalLog> logs = logDAO.findByPatientId(testPatientId);

        assertEquals(2, logs.size());
    }

    @Test
    void findByJsonContains_shouldFindLogsMatchingASpecificField_regardlessOfOtherStructure() throws Exception {
        MedicalLog log1 = new MedicalLog(testPatientId, "{\"severity\": \"high\", \"symptoms\": [\"chest pain\"]}");
        MedicalLog log2 = new MedicalLog(testPatientId, "{\"severity\": \"low\", \"notes\": \"routine follow-up\"}");

        int id1 = logDAO.create(log1);
        int id2 = logDAO.create(log2);
        createdLogIds.add(id1);
        createdLogIds.add(id2);

        List<MedicalLog> highSeverityLogs = logDAO.findByJsonContains("{\"severity\": \"high\"}");

        assertTrue(highSeverityLogs.stream().anyMatch(l -> l.getLogId() == id1));
        assertFalse(highSeverityLogs.stream().anyMatch(l -> l.getLogId() == id2));
    }

    @Test
    void delete_shouldRemoveLog() throws Exception {
        MedicalLog log = new MedicalLog(testPatientId, "{\"type\": \"to_delete\"}");
        int id = logDAO.create(log);

        boolean deleted = logDAO.delete(id);
        List<MedicalLog> remaining = logDAO.findByPatientId(testPatientId);

        assertTrue(deleted);
        assertTrue(remaining.stream().noneMatch(l -> l.getLogId() == id));
    }

    @AfterEach
    void cleanUp() throws Exception {
        for (int id : createdLogIds) {
            logDAO.delete(id);
        }
        createdLogIds.clear();
        patientDAO.delete(testPatientId);
    }
}
package com.hospital.service;

import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.dao.PrescriptionItemDAO;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionItem;

import java.sql.SQLException;
import java.util.List;

public class PrescriptionService {

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final PrescriptionItemDAO itemDAO = new PrescriptionItemDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    public List<Prescription> getAllPrescriptions() throws SQLException {
        return prescriptionDAO.findAllWithNames();
    }

    // Loads a prescription AND populates its items list — the UI shouldn't need two separate calls.
    public Prescription getPrescriptionWithItems(int prescriptionId) throws SQLException {
        Prescription prescription = prescriptionDAO.findById(prescriptionId);
        if (prescription != null) {
            prescription.setItems(itemDAO.findByPrescriptionId(prescriptionId));
        }
        return prescription;
    }

    // Creates the prescription row, then creates each item row pointing back at it.
    // Not wrapped in a single DB transaction yet (each DAO call uses its own connection/auto-commit) —
    // acceptable for this stage of the project, but worth flagging as a place a real system would
    // want transactional atomicity (create prescription + all items, or none of them).
    public int addPrescriptionWithItems(Prescription prescription) throws SQLException {
        validatePrescription(prescription);

        int prescriptionId = prescriptionDAO.create(prescription);

        for (PrescriptionItem item : prescription.getItems()) {
            item.setPrescriptionId(prescriptionId);
            itemDAO.create(item);
        }

        return prescriptionId;
    }

    // Full-replace strategy for items on update: delete all existing items for this prescription,
    // then re-insert whatever's currently in prescription.getItems(). Simpler and less error-prone
    // than trying to diff old vs. new item lists, at the cost of item IDs changing on every update.
    public boolean updatePrescriptionWithItems(Prescription prescription) throws SQLException {
        validatePrescription(prescription);

        boolean updated = prescriptionDAO.update(prescription);
        if (updated) {
            itemDAO.deleteByPrescriptionId(prescription.getPrescriptionId());
            for (PrescriptionItem item : prescription.getItems()) {
                item.setPrescriptionId(prescription.getPrescriptionId());
                itemDAO.create(item);
            }
        }
        return updated;
    }

    // Deleting the prescription cascades to its items at the DB level (ON DELETE CASCADE).
    public boolean deletePrescription(int prescriptionId) throws SQLException {
        return prescriptionDAO.delete(prescriptionId);
    }

    private void validatePrescription(Prescription prescription) throws SQLException {
        if (prescription.getDatePrescribed() == null) {
            throw new IllegalArgumentException("Date prescribed is required.");
        }
        if (patientDAO.findById(prescription.getPatientId()) == null) {
            throw new IllegalArgumentException("Selected patient does not exist.");
        }
        if (doctorDAO.findById(prescription.getDoctorId()) == null) {
            throw new IllegalArgumentException("Selected doctor does not exist.");
        }
        if (prescription.getItems() == null || prescription.getItems().isEmpty()) {
            throw new IllegalArgumentException("A prescription must include at least one medication item.");
        }
        for (PrescriptionItem item : prescription.getItems()) {
            if (item.getMedicationName() == null || item.getMedicationName().trim().isEmpty()) {
                throw new IllegalArgumentException("Each prescription item must have a medication name.");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Each prescription item must have a quantity greater than zero.");
            }
        }
    }
}
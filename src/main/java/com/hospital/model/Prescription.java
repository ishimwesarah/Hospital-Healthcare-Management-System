package com.hospital.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Prescription {

    private int prescriptionId;
    private int patientId;
    private int doctorId;
    private LocalDate datePrescribed;
    private String notes;

    // Display-only fields, populated by joined queries — not persisted directly by the DAO.
    private String patientName;
    private String doctorName;

    // Convenience holder so the Service/UI can pass a prescription and its items around together
    // as one aggregate. PrescriptionDAO does NOT touch this list — only PrescriptionItemDAO does.
    private List<PrescriptionItem> items = new ArrayList<>();

    public Prescription() {
    }

    // Use for a NEW prescription (id not yet known)
    public Prescription(int patientId, int doctorId, LocalDate datePrescribed, String notes) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.datePrescribed = datePrescribed;
        this.notes = notes;
    }

    // Use when reading FROM the database (id is known)
    public Prescription(int prescriptionId, int patientId, int doctorId, LocalDate datePrescribed, String notes) {
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.datePrescribed = datePrescribed;
        this.notes = notes;
    }

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getDatePrescribed() {
        return datePrescribed;
    }

    public void setDatePrescribed(LocalDate datePrescribed) {
        this.datePrescribed = datePrescribed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public List<PrescriptionItem> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "prescriptionId=" + prescriptionId +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", datePrescribed=" + datePrescribed +
                ", notes='" + notes + '\'' +
                ", items=" + items.size() +
                '}';
    }
}